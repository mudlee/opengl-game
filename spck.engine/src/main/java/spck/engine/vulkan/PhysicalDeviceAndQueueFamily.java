package spck.engine.vulkan;

import org.lwjgl.vulkan.VkPhysicalDevice;

class PhysicalDeviceAndQueueFamily {
    private final VkPhysicalDevice physicalDevice;
    private final int graphicsQueueFamilyIndex;

    PhysicalDeviceAndQueueFamily(VkPhysicalDevice physicalDevice, int graphicsQueueFamilyIndex) {
        this.physicalDevice = physicalDevice;
        this.graphicsQueueFamilyIndex = graphicsQueueFamilyIndex;
    }

    public VkPhysicalDevice getPhysicalDevice() {
        return physicalDevice;
    }

    public int getGraphicsQueueFamilyIndex() {
        return graphicsQueueFamilyIndex;
    }
}
