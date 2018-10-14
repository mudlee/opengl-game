package spck.engine.vulkan;

import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.lwjgl.vulkan.KHRSurface.vkDestroySurfaceKHR;
import static org.lwjgl.vulkan.VK10.vkDestroyDevice;
import static org.lwjgl.vulkan.VK11.vkDestroyInstance;

public class VulkanUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(VulkanUtil.class);
    private VkInstance vkInstance;
    private VkDebugger vkDebugger = new VkDebugger();
    private long vkKHRSurface;
    private VkDevice vkDevice;
    private VkQueue vkQueue;

    public void init(int debugFlags, long windowID) {
        LOGGER.debug("Initialising Vulkan...");
        vkInstance = VkInstanceUtil.create();
        vkKHRSurface = VkSurfaceUtil.create(vkInstance, windowID);
        vkDebugger.create(vkInstance, debugFlags);
        PhysicalDeviceAndQueueFamily physicalDeviceAndQueueFamily = VkPhysicalDeviceUtil.pickFirstSuitableDeviceAndQueueFamily(vkInstance, vkKHRSurface);
        vkDevice = VkLogicalDeviceUtil.createDevice(physicalDeviceAndQueueFamily.getGraphicsQueueFamilyIndex(), physicalDeviceAndQueueFamily.getPhysicalDevice());
        vkQueue = VkQueueUtil.create(vkDevice, physicalDeviceAndQueueFamily.getGraphicsQueueFamilyIndex());
        LOGGER.debug("Initialisation done");
    }

    public void cleanup() {
        LOGGER.debug("Cleaning up...");
        LOGGER.debug("- destroying vkDevice...");
        vkDestroyDevice(vkDevice, null);
        LOGGER.debug("- destroying vkSurfaceKHR...");
        vkDestroySurfaceKHR(vkInstance, vkKHRSurface, null);
        LOGGER.debug("- destroying vkDebugger...");
        vkDebugger.cleanup();
        LOGGER.debug("- destroying vkInstance...");
        vkDestroyInstance(vkInstance, null);
        LOGGER.debug("Cleaning up done");
    }
}
