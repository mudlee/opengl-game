package spck.engine.framework;

import org.lwjgl.opengl.GL41;
import spck.engine.render.MeshMaterialBatch;

public class OpenGLPolygonRenderer implements MeshRenderer {
    @Override
    public void render(MeshMaterialBatch batch) {
        GL.vaoContext(batch.getVaoID(), () -> {
            GL41.glEnableVertexAttribArray(OpenGLDefaultGPUMeshDataStore.LayoutQualifier.VX_POSITION.location);

            // 4 slot for the transformation matrix, one for each column
            GL41.glEnableVertexAttribArray(OpenGLDefaultGPUMeshDataStore.LayoutQualifier.INS_TRANSFORMATION_MATRIX_COL1.location);
            GL41.glEnableVertexAttribArray(OpenGLDefaultGPUMeshDataStore.LayoutQualifier.INS_TRANSFORMATION_MATRIX_COL2.location);
            GL41.glEnableVertexAttribArray(OpenGLDefaultGPUMeshDataStore.LayoutQualifier.INS_TRANSFORMATION_MATRIX_COL3.location);
            GL41.glEnableVertexAttribArray(OpenGLDefaultGPUMeshDataStore.LayoutQualifier.INS_TRANSFORMATION_MATRIX_COL4.location);

            GL41.glDrawElementsInstanced(GL41.GL_TRIANGLES, batch.getMesh().getIndices().length, GL41.GL_UNSIGNED_INT, 0, batch.getNumOfEntities());

            GL41.glDisableVertexAttribArray(OpenGLDefaultGPUMeshDataStore.LayoutQualifier.VX_POSITION.location);

            // 4 slot for the transformation matrix, one for each column
            GL41.glDisableVertexAttribArray(OpenGLDefaultGPUMeshDataStore.LayoutQualifier.INS_TRANSFORMATION_MATRIX_COL1.location);
            GL41.glDisableVertexAttribArray(OpenGLDefaultGPUMeshDataStore.LayoutQualifier.INS_TRANSFORMATION_MATRIX_COL2.location);
            GL41.glDisableVertexAttribArray(OpenGLDefaultGPUMeshDataStore.LayoutQualifier.INS_TRANSFORMATION_MATRIX_COL3.location);
            GL41.glDisableVertexAttribArray(OpenGLDefaultGPUMeshDataStore.LayoutQualifier.INS_TRANSFORMATION_MATRIX_COL4.location);
        });
    }
}
