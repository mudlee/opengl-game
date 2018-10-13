package spck.engine.vulkan;

import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.VkExtensionProperties;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;
import org.lwjgl.vulkan.VkQueueFamilyProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.system.MemoryUtil.memAllocInt;
import static org.lwjgl.system.MemoryUtil.memAllocPointer;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfaceSupportKHR;
import static org.lwjgl.vulkan.VK11.VK_QUEUE_GRAPHICS_BIT;
import static org.lwjgl.vulkan.VK11.VK_TRUE;
import static org.lwjgl.vulkan.VK11.vkEnumerateDeviceExtensionProperties;
import static org.lwjgl.vulkan.VK11.vkEnumeratePhysicalDevices;
import static org.lwjgl.vulkan.VK11.vkGetPhysicalDeviceFeatures;
import static org.lwjgl.vulkan.VK11.vkGetPhysicalDeviceProperties;
import static org.lwjgl.vulkan.VK11.vkGetPhysicalDeviceQueueFamilyProperties;
import static spck.engine.vulkan.VkConstants.REQUIRED_DEVICE_EXTENSIONS;

class VkPhysicalDeviceUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(VkPhysicalDeviceUtil.class);

    static PhysicalDeviceAndQueueFamily pickFirstSuitableDeviceAndQueueFamily(VkInstance vkInstance, long vkKHRSurface) {
        LOGGER.debug("Choosing VkPhysicalDevice...");
        IntBuffer deviceCount = memAllocInt(1);
        int result = vkEnumeratePhysicalDevices(vkInstance, deviceCount, null);
        VkResultChecker.check(result, "Failed to get physical devices count.");
        LOGGER.debug("Found {} physical device(s).", deviceCount.get(0));

        if (deviceCount.get(0) == 0) {
            throw new RuntimeException("Failed to find GPUs with Vulkan support.");
        }

        PointerBuffer physicalDevices = memAllocPointer(deviceCount.get(0));
        result = vkEnumeratePhysicalDevices(vkInstance, deviceCount, physicalDevices);
        VkResultChecker.check(result, "Failed to get physical devices.");

        LOGGER.debug("Looking for the first suitable VkPhysicalDevice...");
        VkPhysicalDevice device = null;
        int suitableQueueFamilyIndex = -1;

        for (int deviceIndex = 0; deviceIndex < deviceCount.get(0); deviceIndex++) {
            long devicePtr = physicalDevices.get(deviceIndex);
            device = new VkPhysicalDevice(devicePtr, vkInstance);

            VkPhysicalDeviceProperties vkPhysicalDeviceProperties = VkPhysicalDeviceProperties.calloc();
            vkGetPhysicalDeviceProperties(device, vkPhysicalDeviceProperties);
            String deviceName = vkPhysicalDeviceProperties.deviceNameString();
            vkPhysicalDeviceProperties.free();

            suitableQueueFamilyIndex = findDeviceQueueSupportsGraphicsAndPresentation(deviceIndex, device, vkKHRSurface);
            boolean deviceSupportsGraphicsAndPresentation = suitableQueueFamilyIndex != 1;

            if (suitableQueueFamilyIndex > -1) {
                boolean featuresAvailable = areDeviceFeaturesAvailable(device, deviceIndex);
                boolean extensionsAvailable = areDeviceExtensionsAvailable(device, deviceIndex);

                if (!featuresAvailable) {
                    LOGGER.warn("- No all features are available");
                }

                if (extensionsAvailable && deviceSupportsGraphicsAndPresentation) {
                    LOGGER.debug("Suitable device found. Index: {}, Device: {}", deviceIndex, deviceName);
                    break;
                }
            }
        }

        memFree(deviceCount);
        memFree(physicalDevices);

        if (device == null || suitableQueueFamilyIndex == -1) {
            throw new RuntimeException("No suitable device found.");
        }

        return new PhysicalDeviceAndQueueFamily(device, suitableQueueFamilyIndex);
    }

    private static boolean areDeviceExtensionsAvailable(VkPhysicalDevice device, int deviceIndex) {
        IntBuffer extensionCount = memAllocInt(1);
        int result = vkEnumerateDeviceExtensionProperties(device, (ByteBuffer) null, extensionCount, null);
        VkResultChecker.check(result, "Failed to get device extensions count.");
        VkExtensionProperties.Buffer extensionProperties = VkExtensionProperties.calloc(extensionCount.get(0));
        result = vkEnumerateDeviceExtensionProperties(device, (ByteBuffer) null, extensionCount, extensionProperties);
        VkResultChecker.check(result, "Failed to get physical device extensions' properties.");
        List<String> availableExtensions = new ArrayList<>(extensionCount.get(0));
        for (int i = 0; i < extensionCount.get(0); i++) {
            availableExtensions.add(extensionProperties.get(i).extensionNameString());
        }

        boolean allExtensionsAvailable = true;
        for (String extension : REQUIRED_DEVICE_EXTENSIONS) {
            boolean available = availableExtensions.contains(extension);
            LOGGER.debug("- Device[{}] supports required extension {}: {}", deviceIndex, extension, available);
            if (!available) {
                allExtensionsAvailable = false;
            }
        }
        extensionProperties.free();
        memFree(extensionCount);

        if (!allExtensionsAvailable) {
            LOGGER.warn("- Device[{}]: Not all required extensions are available for this device", deviceIndex);
        }

        return allExtensionsAvailable;
    }

    private static boolean areDeviceFeaturesAvailable(VkPhysicalDevice device, int deviceIndex) {
        VkPhysicalDeviceFeatures features = VkPhysicalDeviceFeatures.calloc();
        vkGetPhysicalDeviceFeatures(device, features);
        boolean geometryShaders = features.geometryShader();
        LOGGER.debug("- Device[{}] supports required feature GEOMETRYSHADER support: {}", deviceIndex, geometryShaders);
        features.free();

        if (!geometryShaders) {
            LOGGER.warn("- Device[{}]: GeometryShaders feature is not available", deviceIndex);
        }

        return geometryShaders;
    }

    private static int findDeviceQueueSupportsGraphicsAndPresentation(int deviceIndex, VkPhysicalDevice physicalDevice, long vkKHRSurface) {
        LOGGER.debug("- Device[{}] checking device queues", deviceIndex);
        IntBuffer queueFamilyPropertyCount = memAllocInt(1);
        vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, queueFamilyPropertyCount, null);

        VkQueueFamilyProperties.Buffer queueProps = VkQueueFamilyProperties.calloc(queueFamilyPropertyCount.get(0));
        vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, queueFamilyPropertyCount, queueProps);

        boolean graphicsSupport = false;
        boolean presentationSupport = false;

        int suitableQueueFamilyIndex = -1;

        for (int index = 0; index < queueFamilyPropertyCount.get(0); index++) {
            LOGGER.debug("- Device[{}] checking queue family {}", index);
            VkQueueFamilyProperties queueFamilyProperties = queueProps.get(index);
            boolean queueFamilyFound = false;

            if ((queueFamilyProperties.queueFlags() & VK_QUEUE_GRAPHICS_BIT) != 0) {
                graphicsSupport = true;

                // Iterate over each queue to learn whether it supports presenting:
                IntBuffer supportsPresent = memAllocInt(queueFamilyProperties.queueCount());
                for (int i = 0; i < queueFamilyProperties.queueCount(); i++) {
                    supportsPresent.position(i);
                    int result = vkGetPhysicalDeviceSurfaceSupportKHR(physicalDevice, i, vkKHRSurface, supportsPresent);
                    VkResultChecker.check(result, "Failed to get physical device support.");

                    if (supportsPresent.get(i) == VK_TRUE) {
                        presentationSupport = true;
                        suitableQueueFamilyIndex = index;
                        queueFamilyFound = true;
                        break;
                    }
                }

                memFree(supportsPresent);
            }

            if (queueFamilyFound) {
                break;
            }
        }

        LOGGER.debug("- Device[{}] supports required VK_QUEUE_GRAPHICS_BIT: {}", deviceIndex, graphicsSupport);
        LOGGER.debug("- Device[{}] supports required presentation: {}", deviceIndex, presentationSupport);

        memFree(queueFamilyPropertyCount);
        queueProps.free();

        return suitableQueueFamilyIndex;
    }
}
