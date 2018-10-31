module spck.engine {
    requires org.lwjgl;
    requires org.lwjgl.natives;
    requires transitive org.lwjgl.glfw;
    requires org.lwjgl.glfw.natives;
    requires org.lwjgl.opengl;
    requires org.lwjgl.opengl.natives;
    requires org.lwjgl.nanovg;
    requires org.lwjgl.nanovg.natives;
    requires org.lwjgl.stb;
    requires org.lwjgl.stb.natives;
    requires org.lwjgl.assimp;
    requires org.lwjgl.assimp.natives;
    requires org.slf4j;
    requires org.slf4j.simple;
    requires artemis.odb;
    requires transitive org.joml;

    opens spck.engine.ecs to artemis.odb;
    opens spck.engine.ecs.debug to artemis.odb;
    opens spck.engine.ecs.ui to artemis.odb;
    opens spck.engine.ecs.render to artemis.odb;

    exports spck.engine;
    exports spck.engine.bus;
    exports spck.engine.debug;
    exports spck.engine.ecs.render.components;
    exports spck.engine.lights;
    exports spck.engine.model.primitives;
    exports spck.engine.render;
    exports spck.engine.render.textures;
}