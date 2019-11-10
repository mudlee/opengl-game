package spck.engine.framework;

import org.lwjgl.opengl.GL41;
import spck.engine.render.MeshMaterialBatch;

import static spck.engine.framework.OpenGLAABBGPUDataStore.NUMBER_OF_INDICES_PER_AABB;

public class OpenGLAABBRenderer implements Renderer {
    @Override
    public void render(MeshMaterialBatch batch) {
        GL.vaoContext(batch.getAABBVaoID(), () -> {
            GL41.glEnableVertexAttribArray(OpenGLAABBGPUDataStore.LayoutQualifier.VX_POSITION.location);
            GL41.glDrawElements(GL41.GL_TRIANGLES, batch.getNumOfEntities() * NUMBER_OF_INDICES_PER_AABB, GL41.GL_UNSIGNED_INT, 0);
            GL41.glDisableVertexAttribArray(OpenGLAABBGPUDataStore.LayoutQualifier.VX_POSITION.location);
        });
    }
}
