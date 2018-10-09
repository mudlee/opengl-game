package spck.engine.vulkan;

import org.lwjgl.vulkan.VkInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spck.engine.core.OS;

public class VulkanUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(VulkanUtil.class);

    public void init(@SuppressWarnings("ClassEscapesDefinedScope") OS os) {
        LOGGER.debug("Initialising Vulkan...");

        boolean debugLayerEnabled = os != OS.OSX;

        VkInstance vkInstance = VkInstanceUtil.create(debugLayerEnabled);
        LOGGER.debug("Initialisation done");
    }

    public void cleanup() {
        LOGGER.debug("Cleaning up...");
        LOGGER.debug("Cleaning up done");
    }
}
