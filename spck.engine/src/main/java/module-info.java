module spck.engine {
    requires org.lwjgl;
    requires org.lwjgl.natives;
    requires org.lwjgl.glfw;
    requires org.lwjgl.vulkan;
    requires org.slf4j;

    exports spck.engine;
    exports spck.engine.vulkan;
}