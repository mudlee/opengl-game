package spck.engine.vulkan;

import org.lwjgl.vulkan.VkInstance;

import java.nio.LongBuffer;

import static org.lwjgl.glfw.GLFWVulkan.glfwCreateWindowSurface;
import static org.lwjgl.system.MemoryUtil.memAllocLong;
import static org.lwjgl.system.MemoryUtil.memFree;

class VkSurfaceUtil {
    public static long create(VkInstance vkInstance, long windowID) {
        LongBuffer pSurface = memAllocLong(1);
        int result = glfwCreateWindowSurface(vkInstance, windowID, null, pSurface);
        VkResultChecker.check(result, "Failed to create VkSurface.");

        final long surface = pSurface.get(0);

        memFree(pSurface);
        return surface;
    }
}
