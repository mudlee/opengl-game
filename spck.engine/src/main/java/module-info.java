module spck.engine {
    requires org.lwjgl;
    requires org.lwjgl.natives;
    requires org.lwjgl.glfw;
    requires org.lwjgl.glfw.natives;
    requires org.slf4j;
    requires org.slf4j.simple;
    requires org.lwjgl.opengl;
    requires org.lwjgl.opengl.natives;
    requires artemis.odb;

    exports spck.engine;
    exports spck.engine.core;
}