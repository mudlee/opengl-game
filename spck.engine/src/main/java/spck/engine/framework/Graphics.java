package spck.engine.framework;

import org.lwjgl.opengl.GL41;

public class Graphics {
    public enum PolygonMode {
        FILL(GL41.GL_FILL), LINE(GL41.GL_LINE), POINT(GL41.GL_POINT);

        private int glId;

        PolygonMode(int glId) {
            this.glId = glId;
        }
    }

    public static void setPolygonMode(PolygonMode mode) {
        GL41.glPolygonMode(GL41.GL_FRONT_AND_BACK, mode.glId);
    }

    public static void clearScreen(float r, float g, float b, float a) {
        GL41.glClearColor(r, g, b, a);
        GL41.glClear(GL41.GL_COLOR_BUFFER_BIT | GL41.GL_DEPTH_BUFFER_BIT | GL41.GL_STENCIL_BUFFER_BIT);
    }
}
