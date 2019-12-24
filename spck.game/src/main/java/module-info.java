module spck.game {
    requires spck.engine;
    requires org.lwjgl.nanovg;
    requires org.lwjgl.opengl;
    requires org.lwjgl.stb;
    opens spck.game.ui to artemis.odb;
    exports spck.game to spck.engine;
}