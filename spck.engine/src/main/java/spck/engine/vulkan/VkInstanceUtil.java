package spck.engine.vulkan;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.*;

import static org.lwjgl.glfw.GLFWVulkan.glfwGetRequiredInstanceExtensions;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.EXTDebugReport.VK_EXT_DEBUG_REPORT_EXTENSION_NAME;
import static org.lwjgl.vulkan.EXTDebugUtils.VK_EXT_DEBUG_UTILS_EXTENSION_NAME;
import static org.lwjgl.vulkan.VK11.*;

/**
 * The instance is the connection between your application and the Vulkan library and creating it involves
 * specifying some details about your application to the driver.
 *
 * @see <a href="https://vulkan-tutorial.com/Drawing_a_triangle/Setup/Instance">https://vulkan-tutorial.com/Drawing_a_triangle/Setup/Instance</a>
 */
class VkInstanceUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(VkInstanceUtil.class);

    static VkInstance create() {
        int result;

        LOGGER.debug("Creating VkApplicationInfo...");
        VkApplicationInfo vkAppInfo = VkApplicationInfo.calloc().
                sType(VK_STRUCTURE_TYPE_APPLICATION_INFO).
                pApplicationName(memUTF8("SPCK")).
                applicationVersion(VK_MAKE_VERSION(1, 0, 0)).
                pEngineName(memUTF8("SPCK")).
                engineVersion(VK_MAKE_VERSION(1, 0, 0)).
                apiVersion(VK_API_VERSION_1_0);

        checkValidationLayers();

        Optional<PointerBuffer> ppEnabledLayerNames = constructValidationLayers();
        Map<String, ByteBuffer> extensionBuffers = constructExtensionBuffers();
        PointerBuffer extensionNames = constructExtensionNames(extensionBuffers);

        LOGGER.debug("Creating VkInstanceCreateInfo...");
        VkInstanceCreateInfo vkInstanceCreateInfo = VkInstanceCreateInfo.calloc().
                sType(VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO).
                pNext(NULL).
                pApplicationInfo(vkAppInfo);

        ppEnabledLayerNames.ifPresent(vkInstanceCreateInfo::ppEnabledLayerNames);
        vkInstanceCreateInfo.ppEnabledExtensionNames(extensionNames);

        LOGGER.debug("Creating vkInstance...");
        PointerBuffer pInstance = memAllocPointer(1);
        result = vkCreateInstance(vkInstanceCreateInfo, null, pInstance);
        VkResultChecker.check(result, "Unabled to create VkInstance");
        VkInstance vkInstance = new VkInstance(pInstance.get(0), vkInstanceCreateInfo);

        LOGGER.debug("vkInstance created, cleaning up...");
        memFree(pInstance);
        ppEnabledLayerNames.ifPresent(MemoryUtil::memFree);
        extensionBuffers.values().forEach(MemoryUtil::memFree);
        memFree(extensionNames);
        memFree(vkAppInfo.pApplicationName());
        memFree(vkAppInfo.pEngineName());
        vkAppInfo.free();
        vkInstanceCreateInfo.free();
        LOGGER.debug("Cleaned up");

        return vkInstance;
    }

    private static void checkValidationLayers() {
        LOGGER.debug("Checking validation layers...");
        IntBuffer validationLayerCount = memAllocInt(1);
        int result = vkEnumerateInstanceLayerProperties(validationLayerCount, null);
        VkResultChecker.check(result, "Unabled to get validation layers' count.");
        LOGGER.debug("There are {} available validation layers:", validationLayerCount.get(0));

        VkLayerProperties.Buffer layerProperties = VkLayerProperties.calloc(validationLayerCount.get(0));
        result = vkEnumerateInstanceLayerProperties(validationLayerCount, layerProperties);
        VkResultChecker.check(result, "Unabled to get validation layers.");
        for (int i = 0; i < validationLayerCount.get(0); i++) {
            VkLayerProperties properties = layerProperties.get(i);
            LOGGER.debug("- {}, ver: {}, desc: {}", properties.layerNameString(), properties.specVersion(), properties.descriptionString());
        }
        layerProperties.free();
        memFree(validationLayerCount);
    }

    private static PointerBuffer constructExtensionNames(Map<String, ByteBuffer> extensionBuffers) {
        LOGGER.debug("Constructing extension names...");

        PointerBuffer requiredExtensions = glfwGetRequiredInstanceExtensions();
        if (requiredExtensions == null) {
            throw new RuntimeException("GLFW failed to find list of required Vulkan extensions");
        }

        // check for available extensions
        IntBuffer extensionCount = memAllocInt(1);
        int result = vkEnumerateInstanceExtensionProperties((ByteBuffer) null, extensionCount, null);
        VkResultChecker.check(result, "Unabled to query Vulkan extension count.");

        LOGGER.debug("There are {} available extensions:", extensionCount.get(0));
        List<String> availableExtensionNames = new ArrayList<>(extensionCount.get(0));
        VkExtensionProperties.Buffer extensionProperties = VkExtensionProperties.calloc(extensionCount.get(0));
        result = vkEnumerateInstanceExtensionProperties((ByteBuffer) null, extensionCount, extensionProperties);
        VkResultChecker.check(result, "Unabled to query Vulkan extension properties.");
        for (int i = 0; i < extensionCount.get(0); i++) {
            VkExtensionProperties properties = extensionProperties.get(i);
            availableExtensionNames.add(properties.extensionNameString());
            LOGGER.debug("- {}, ver: {}", properties.extensionNameString(), properties.specVersion());
        }
        memFree(extensionCount);
        extensionProperties.free();

        LOGGER.debug("Adding extensions");
        PointerBuffer ppEnabledExtensionNames = memAllocPointer(requiredExtensions.remaining() + 1);
        ppEnabledExtensionNames.put(requiredExtensions);
        LOGGER.debug("- required extensions added");

        for (Map.Entry<String, ByteBuffer> entry : extensionBuffers.entrySet()) {
            String extensionName = entry.getKey();
            ByteBuffer extensionBuffer = entry.getValue();

            if (availableExtensionNames.contains(extensionName)) {
                ppEnabledExtensionNames.put(extensionBuffer);
                LOGGER.debug("- added: {}", extensionName);
            } else {
                LOGGER.debug("- failed: {}", extensionName);
            }
        }

        ppEnabledExtensionNames.flip();
        return ppEnabledExtensionNames;
    }

    private static Map<String, ByteBuffer> constructExtensionBuffers() {
        LOGGER.debug("Constructing extension buffers...");

        Map<String, ByteBuffer> extensionBuffers = new HashMap<>();

        // debug extensions
        extensionBuffers.put(VK_EXT_DEBUG_REPORT_EXTENSION_NAME, memUTF8(VK_EXT_DEBUG_REPORT_EXTENSION_NAME));
        extensionBuffers.put(VK_EXT_DEBUG_UTILS_EXTENSION_NAME, memUTF8(VK_EXT_DEBUG_REPORT_EXTENSION_NAME));

        LOGGER.debug("- Added extension: {}", VK_EXT_DEBUG_REPORT_EXTENSION_NAME);
        LOGGER.debug("- Added extension: {}", VK_EXT_DEBUG_UTILS_EXTENSION_NAME);

        return extensionBuffers;
    }

    /**
     * Vulkan introduces an elegant system for this known as validation layers. Validation layers are optional
     * components that hook into Vulkan function calls to apply additional operations.
     *
     * @see <a href="https://vulkan-tutorial.com/Drawing_a_triangle/Setup/Validation_layers">https://vulkan-tutorial.com/Drawing_a_triangle/Setup/Validation_layers</a>
     */
    private static Optional<PointerBuffer> constructValidationLayers() {
        LOGGER.debug("Constructing validation layers: {}", VkConstants.VALIDATION_ENABLED);

        if (!VkConstants.VALIDATION_ENABLED) {
            LOGGER.debug("- vulkan.validation system property is not true");
            return Optional.empty();
        }

        PointerBuffer layers = memAllocPointer(VkConstants.VALIDATION_LAYERS.length);
        for (int i = 0; i < VkConstants.VALIDATION_LAYERS.length; i++) {
            LOGGER.debug("- Adding layer: {}", VkConstants.VALIDATION_LAYERS[i]);
            layers.put(VkConstants.VALIDATION_LAYERS[i]);
        }
        layers.flip();
        LOGGER.debug("- DONE");
        return Optional.of(layers);
    }
}
