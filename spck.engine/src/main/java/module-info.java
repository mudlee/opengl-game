module spck.engine {
    requires org.lwjgl;
    requires org.lwjgl.natives;
    requires org.lwjgl.glfw;
    requires org.lwjgl.glfw.natives;
    requires org.lwjgl.vulkan;
    requires org.lwjgl.vulkan.natives;
    requires org.slf4j;

    exports spck.engine;
    exports spck.engine.vulkan;
}