package spck.engine.vulkan;

import org.lwjgl.vulkan.VkInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.lwjgl.vulkan.KHRSurface.vkDestroySurfaceKHR;
import static org.lwjgl.vulkan.VK11.vkDestroyInstance;

public class VulkanUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(VulkanUtil.class);
    private VkInstance vkInstance;
    private VkDebugger vkDebugger = new VkDebugger();
    private long vkKHRSurface;

    public void init(int debugFlags, long windowID) {
        LOGGER.debug("Initialising Vulkan...");
        vkInstance = VkInstanceUtil.create();
        vkKHRSurface = VkSurfaceUtil.create(vkInstance, windowID);
        vkDebugger.create(vkInstance, debugFlags);
        PhysicalDeviceAndQueueFamily physicalDeviceAndQueueFamily = VkPhysicalDeviceUtil.pickFirstSuitableDeviceAndQueueFamily(vkInstance, vkKHRSurface);
        LOGGER.debug("Initialisation done");
    }

    public void cleanup() {
        LOGGER.debug("Cleaning up...");
        LOGGER.debug("- destroying vkDevice...");
        //vkDestroyDevice(vkDevice, null); TODO
        LOGGER.debug("- destroying vkSurfaceKHR...");
        vkDestroySurfaceKHR(vkInstance, vkKHRSurface, null);
        LOGGER.debug("- destroying vkDebugger...");
        vkDebugger.cleanup();
        LOGGER.debug("- destroying vkInstance...");
        vkDestroyInstance(vkInstance, null);
        LOGGER.debug("Cleaning up done");
    }
}
