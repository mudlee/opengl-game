module spck.engine {
    requires org.lwjgl;
    requires org.lwjgl.natives;
    requires org.lwjgl.glfw;
    requires org.lwjgl.glfw.natives;
    requires org.lwjgl.opengl;
    requires org.lwjgl.opengl.natives;
    requires org.joml;
    requires org.lwjgl.nanovg;
    requires org.lwjgl.nanovg.natives;
    requires org.slf4j;
    requires org.slf4j.simple;
    requires artemis.odb;

    opens spck.engine.framework.ecs to artemis.odb;
    opens spck.engine.debug.ecs to artemis.odb;
    opens spck.engine.ui.ecs to artemis.odb;

    exports spck.engine;
}