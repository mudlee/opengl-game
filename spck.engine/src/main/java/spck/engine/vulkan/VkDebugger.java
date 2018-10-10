package spck.engine.vulkan;

import org.lwjgl.vulkan.VkDebugReportCallbackCreateInfoEXT;
import org.lwjgl.vulkan.VkDebugReportCallbackEXT;
import org.lwjgl.vulkan.VkInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.LongBuffer;

import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.system.MemoryUtil.memAllocLong;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.vulkan.EXTDebugReport.VK_STRUCTURE_TYPE_DEBUG_REPORT_CALLBACK_CREATE_INFO_EXT;
import static org.lwjgl.vulkan.EXTDebugReport.vkCreateDebugReportCallbackEXT;
import static org.lwjgl.vulkan.EXTDebugReport.vkDestroyDebugReportCallbackEXT;

class VkDebugger {
    private static final Logger LOGGER = LoggerFactory.getLogger(VkDebugger.class);
    private VkInstance instance;
    private long callbackHandle;
    private boolean created;

    void create(VkInstance instance, int flags) {
        LOGGER.debug("Creating {}", VkDebugger.class.getSimpleName());
        this.instance = instance;

        if (!instance.getCapabilities().VK_EXT_debug_report) {
            LOGGER.warn("{} cannot be setup, as vkInstance does not have VK_EXT_debug_report enabled", VkDebugger.class.getSimpleName());
            return;
        }

        final VkDebugReportCallbackEXT debugCallback = new VkDebugReportCallbackEXT() {
            public int invoke(int flags, int objectType, long object, long location, int messageCode, long pLayerPrefix, long pMessage, long pUserData) {
                LOGGER.error("{}", VkDebugReportCallbackEXT.getString(pMessage));
                return 0;
            }
        };

        VkDebugReportCallbackCreateInfoEXT dbgCreateInfo = VkDebugReportCallbackCreateInfoEXT.calloc()
                .sType(VK_STRUCTURE_TYPE_DEBUG_REPORT_CALLBACK_CREATE_INFO_EXT)
                .pNext(NULL)
                .pfnCallback(debugCallback)
                .pUserData(NULL)
                .flags(flags);
        LongBuffer pCallback = memAllocLong(1);
        int result = vkCreateDebugReportCallbackEXT(instance, dbgCreateInfo, null, pCallback);
        callbackHandle = pCallback.get(0);
        memFree(pCallback);
        dbgCreateInfo.free();

        VkResultChecker.check(result, "Failed to setup debugging");
        created = true;
        LOGGER.debug("- created");
    }

    void cleanup() {
        if (created) {
            LOGGER.debug("Cleaning up {}", VkDebugger.class.getSimpleName());
            vkDestroyDebugReportCallbackEXT(instance, callbackHandle, null);
            LOGGER.debug("- cleaned");
        }
    }
}
