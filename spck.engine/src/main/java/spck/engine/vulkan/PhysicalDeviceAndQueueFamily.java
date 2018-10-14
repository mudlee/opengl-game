package spck.engine.vulkan;

import org.lwjgl.vulkan.VkPhysicalDevice;

class PhysicalDeviceAndQueueFamily {
    private final VkPhysicalDevice physicalDevice;
    private final int graphicsQueueFamilyIndex;

    PhysicalDeviceAndQueueFamily(VkPhysicalDevice physicalDevice, int graphicsQueueFamilyIndex) {
        this.physicalDevice = physicalDevice;
        this.graphicsQueueFamilyIndex = graphicsQueueFamilyIndex;
    }

    VkPhysicalDevice getPhysicalDevice() {
        return physicalDevice;
    }

    int getGraphicsQueueFamilyIndex() {
        return graphicsQueueFamilyIndex;
    }
}
