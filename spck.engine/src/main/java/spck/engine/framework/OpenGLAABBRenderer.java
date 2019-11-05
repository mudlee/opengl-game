package spck.engine.framework;

import org.lwjgl.opengl.GL41;
import spck.engine.render.LayoutQualifier;
import spck.engine.render.MeshMaterialBatch;

public class OpenGLAABBRenderer implements Renderer {
    @Override
    public void render(MeshMaterialBatch batch) {
        GL.vaoContext(batch.getVaoID(), () -> {
            GL41.glEnableVertexAttribArray(LayoutQualifier.VX_POSITION.location);

            // 4 slot for the transformation matrix, one for each column
            GL41.glEnableVertexAttribArray(LayoutQualifier.INS_TRANSFORMATION_MATRIX_COL1.location);
            GL41.glEnableVertexAttribArray(LayoutQualifier.INS_TRANSFORMATION_MATRIX_COL2.location);
            GL41.glEnableVertexAttribArray(LayoutQualifier.INS_TRANSFORMATION_MATRIX_COL3.location);
            GL41.glEnableVertexAttribArray(LayoutQualifier.INS_TRANSFORMATION_MATRIX_COL4.location);

            GL41.glDrawElementsInstanced(GL41.GL_TRIANGLES, batch.getMesh().getIndices().length, GL41.GL_UNSIGNED_INT, 0, batch.getNumOfEntities());

            GL41.glDisableVertexAttribArray(LayoutQualifier.VX_POSITION.location);

            // 4 slot for the transformation matrix, one for each column
            GL41.glDisableVertexAttribArray(LayoutQualifier.INS_TRANSFORMATION_MATRIX_COL1.location);
            GL41.glDisableVertexAttribArray(LayoutQualifier.INS_TRANSFORMATION_MATRIX_COL2.location);
            GL41.glDisableVertexAttribArray(LayoutQualifier.INS_TRANSFORMATION_MATRIX_COL3.location);
            GL41.glDisableVertexAttribArray(LayoutQualifier.INS_TRANSFORMATION_MATRIX_COL4.location);
        });
    }
}
