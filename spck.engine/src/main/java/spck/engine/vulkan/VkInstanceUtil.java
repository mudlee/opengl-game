package spck.engine.vulkan;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkExtensionProperties;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkInstanceCreateInfo;
import org.lwjgl.vulkan.VkLayerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Optional;

import static org.lwjgl.glfw.GLFWVulkan.glfwGetRequiredInstanceExtensions;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.system.MemoryUtil.memAllocInt;
import static org.lwjgl.system.MemoryUtil.memAllocPointer;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.system.MemoryUtil.memUTF8;
import static org.lwjgl.vulkan.EXTDebugReport.VK_EXT_DEBUG_REPORT_EXTENSION_NAME;
import static org.lwjgl.vulkan.VK11.VK_MAKE_VERSION;
import static org.lwjgl.vulkan.VK11.VK_STRUCTURE_TYPE_APPLICATION_INFO;
import static org.lwjgl.vulkan.VK11.VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO;
import static org.lwjgl.vulkan.VK11.vkCreateInstance;
import static org.lwjgl.vulkan.VK11.vkEnumerateInstanceExtensionProperties;
import static org.lwjgl.vulkan.VK11.vkEnumerateInstanceLayerProperties;

/**
 * The instance is the connection between your application and the Vulkan library and creating it involves
 * specifying some details about your application to the driver.
 *
 * @see <a href="https://vulkan-tutorial.com/Drawing_a_triangle/Setup/Instance">https://vulkan-tutorial.com/Drawing_a_triangle/Setup/Instance</a>
 */
class VkInstanceUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(VkInstanceUtil.class);

    static VkInstance create(boolean enableDebug) {
        int result;

        LOGGER.debug("Creating VkApplicationInfo...");
        VkApplicationInfo vkAppInfo = VkApplicationInfo.calloc().
                sType(VK_STRUCTURE_TYPE_APPLICATION_INFO).
                pApplicationName(memUTF8("Vulkan Application")).
                applicationVersion(VK_MAKE_VERSION(1, 0, 2)).
                pEngineName(memUTF8("Intermetto")).
                engineVersion(VK_MAKE_VERSION(1, 0, 0)).
                apiVersion(VK_MAKE_VERSION(1, 0, 2));

        checkValidationLayers();

        Optional<PointerBuffer> ppEnabledLayerNames = constructValidationLayers();
        Optional<ByteBuffer[]> extensionBuffers = constructExtensionBuffers(enableDebug);
        Optional<PointerBuffer> extensionNames = constructExtensionNames(extensionBuffers.orElse(null));

        LOGGER.debug("Creating VkInstanceCreateInfo...");
        VkInstanceCreateInfo vkInstanceCreateInfo = VkInstanceCreateInfo.calloc().
                sType(VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO).
                pNext(NULL).
                pApplicationInfo(vkAppInfo);

        ppEnabledLayerNames.ifPresent(vkInstanceCreateInfo::ppEnabledLayerNames);
        extensionNames.ifPresent(vkInstanceCreateInfo::ppEnabledExtensionNames);

        LOGGER.debug("Creating vkInstance...");
        PointerBuffer pInstance = memAllocPointer(1);
        result = vkCreateInstance(vkInstanceCreateInfo, null, pInstance);
        VkResultChecker.check(result, "Unabled to create VkInstance");
        VkInstance vkInstance = new VkInstance(pInstance.get(0), vkInstanceCreateInfo);

        LOGGER.debug("vkInstance created, cleaning up...");
        memFree(pInstance);
        ppEnabledLayerNames.ifPresent(MemoryUtil::memFree);
        extensionBuffers.ifPresent(byteBuffers -> {
            for (ByteBuffer byteBuffer : byteBuffers) {
                memFree(byteBuffer);
            }
        });
        extensionNames.ifPresent(MemoryUtil::memFree);
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

    private static Optional<PointerBuffer> constructExtensionNames(ByteBuffer[] extensionBuffers) {
        LOGGER.debug("Constructing extension names...");
        if (extensionBuffers == null) {
            LOGGER.debug("- extensionBuffers is null, no extension name was added");
            return Optional.empty();
        }

        PointerBuffer requiredExtensions = glfwGetRequiredInstanceExtensions();
        if (requiredExtensions == null) {
            throw new RuntimeException("GLFW failed to find list of required Vulkan extensions");
        }

        // check for available extensions
        IntBuffer extensionCount = memAllocInt(1);
        int result = vkEnumerateInstanceExtensionProperties((ByteBuffer) null, extensionCount, null);
        VkResultChecker.check(result, "Unabled to query Vulkan extension count.");

        LOGGER.debug("There are {} available extensions:", extensionCount.get(0));
        VkExtensionProperties.Buffer extensionProperties = VkExtensionProperties.calloc(extensionCount.get(0));
        result = vkEnumerateInstanceExtensionProperties((ByteBuffer) null, extensionCount, extensionProperties);
        VkResultChecker.check(result, "Unabled to query Vulkan extension properties.");
        for (int i = 0; i < extensionCount.get(0); i++) {
            VkExtensionProperties properties = extensionProperties.get(i);
            LOGGER.debug("- {}, ver: {}", properties.extensionNameString(), properties.specVersion());
        }
        memFree(extensionCount);
        extensionProperties.free();

        // adding extensions
        PointerBuffer ppEnabledExtensionNames = memAllocPointer(requiredExtensions.remaining() + 1);
        ppEnabledExtensionNames.put(requiredExtensions);

        for (ByteBuffer extensionBuffer : extensionBuffers) {
            ppEnabledExtensionNames.put(extensionBuffer);
        }

        ppEnabledExtensionNames.flip();
        LOGGER.debug("- DONE");
        return Optional.of(ppEnabledExtensionNames);
    }

    private static Optional<ByteBuffer[]> constructExtensionBuffers(boolean enableDebug) {
        LOGGER.debug("Constructing extension buffers...");
        if (!enableDebug) {
            LOGGER.debug("- debug is not enabled, no extension was added");
            return Optional.empty();
        }

        ByteBuffer[] extensionBuffers = new ByteBuffer[]{
                memUTF8(VK_EXT_DEBUG_REPORT_EXTENSION_NAME)
        };

        LOGGER.debug("- Added extension: {}", VK_EXT_DEBUG_REPORT_EXTENSION_NAME);

        return Optional.of(extensionBuffers);
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
            LOGGER.debug("- vulkan.validation is not enabled");
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
