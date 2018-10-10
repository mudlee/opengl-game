package spck.engine.vulkan;

import org.lwjgl.vulkan.VkInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VulkanUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(VulkanUtil.class);

    public void init() {
        LOGGER.debug("Initialising Vulkan...");

        VkInstance vkInstance = VkInstanceUtil.create();
        LOGGER.debug("Initialisation done");
    }

    public void cleanup() {
        LOGGER.debug("Cleaning up...");
        LOGGER.debug("Cleaning up done");
    }
}
