package spck.engine.vulkan;

import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkDeviceCreateInfo;
import org.lwjgl.vulkan.VkDeviceQueueCreateInfo;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.system.MemoryUtil.memAllocFloat;
import static org.lwjgl.system.MemoryUtil.memAllocPointer;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.system.MemoryUtil.memUTF8;
import static org.lwjgl.vulkan.KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME;
import static org.lwjgl.vulkan.VK11.VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO;
import static org.lwjgl.vulkan.VK11.VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO;
import static org.lwjgl.vulkan.VK11.vkCreateDevice;
import static org.lwjgl.vulkan.VK11.vkGetPhysicalDeviceMemoryProperties;

class VkLogicalDeviceUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(VkLogicalDeviceUtil.class);

    static VkDevice createDevice(int graphicsQueueFamilyIndex, VkPhysicalDevice vkPhysicalDevice) {
        LOGGER.debug("Creating logical device...");
        FloatBuffer pQueuePriorities = memAllocFloat(1).put(0.0f);
        pQueuePriorities.flip();
        VkDeviceQueueCreateInfo.Buffer queueCreateInfo = VkDeviceQueueCreateInfo.calloc(1)
                .sType(VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO)
                .queueFamilyIndex(graphicsQueueFamilyIndex)
                .pQueuePriorities(pQueuePriorities);

        PointerBuffer extensions = memAllocPointer(1);
        ByteBuffer VK_KHR_SWAPCHAIN_EXTENSION = memUTF8(VK_KHR_SWAPCHAIN_EXTENSION_NAME);
        extensions.put(VK_KHR_SWAPCHAIN_EXTENSION);
        extensions.flip();

        PointerBuffer layers = memAllocPointer(VkConstants.VALIDATION_LAYERS.length);
        for (int i = 0; VkConstants.VALIDATION_ENABLED && i < VkConstants.VALIDATION_LAYERS.length; i++) {
            layers.put(VkConstants.VALIDATION_LAYERS[i]);
        }
        layers.flip();

        VkDeviceCreateInfo deviceCreateInfo = VkDeviceCreateInfo.calloc()
                .sType(VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO)
                .pNext(NULL)
                .pQueueCreateInfos(queueCreateInfo)
                .ppEnabledExtensionNames(extensions)
                .ppEnabledLayerNames(layers);

        PointerBuffer pDevice = memAllocPointer(1);
        int result = vkCreateDevice(vkPhysicalDevice, deviceCreateInfo, null, pDevice);
        VkResultChecker.check(result, "Failed to creat logical device.");
        long devicePtr = pDevice.get(0);

        VkPhysicalDeviceMemoryProperties memoryProperties = VkPhysicalDeviceMemoryProperties.calloc();
        vkGetPhysicalDeviceMemoryProperties(vkPhysicalDevice, memoryProperties);

        VkDevice vkDevice = new VkDevice(devicePtr, vkPhysicalDevice, deviceCreateInfo);

        memFree(pDevice);
        queueCreateInfo.free();
        deviceCreateInfo.free();
        memoryProperties.free();
        memFree(layers);
        memFree(VK_KHR_SWAPCHAIN_EXTENSION);
        memFree(extensions);
        memFree(pQueuePriorities);
        return vkDevice;
    }
}
