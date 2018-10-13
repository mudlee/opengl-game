package spck.engine.vulkan;

import java.nio.ByteBuffer;

import static org.lwjgl.system.MemoryUtil.memUTF8;
import static org.lwjgl.vulkan.KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME;
import static org.lwjgl.vulkan.VK11.VK_QUEUE_GRAPHICS_BIT;

class VkConstants {
    static final boolean VALIDATION_ENABLED = Boolean.parseBoolean(System.getProperty("vulkan.validation", "false"));
    static ByteBuffer[] VALIDATION_LAYERS = {memUTF8("VK_LAYER_LUNARG_standard_validation")};
    static final String[] REQUIRED_DEVICE_EXTENSIONS = {VK_KHR_SWAPCHAIN_EXTENSION_NAME};
    static final int[] REQUIRED_DEVICE_QUEUE_FAMILY_FLAGS = new int[]{VK_QUEUE_GRAPHICS_BIT};
}
