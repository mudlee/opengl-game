package spck.engine.vulkan;

import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.lwjgl.system.MemoryUtil.memAllocPointer;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.vulkan.VK11.vkGetDeviceQueue;

class VkQueueUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(VkQueueUtil.class);

    static VkQueue getDeviceQueue(VkDevice vkDevice, int graphicsQueueFamilyIndex) {
        LOGGER.debug("Creating queue...");
        PointerBuffer pQueue = memAllocPointer(1);
        vkGetDeviceQueue(vkDevice, graphicsQueueFamilyIndex, 0, pQueue);
        long queue = pQueue.get(0);
        memFree(pQueue);

        return new VkQueue(queue, vkDevice);
    }
}
