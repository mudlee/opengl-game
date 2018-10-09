package spck.engine.vulkan;

import static org.lwjgl.vulkan.VK11.VK_SUCCESS;

class VkResultChecker {
    static void check(int result, String errorMessage) {
        if (result != VK_SUCCESS) {
            throw new RuntimeException(errorMessage + " - " + VkErrorCode.translate(result));
        }
    }
}
