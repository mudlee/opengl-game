package spck.engine.framework;

import org.lwjgl.opengl.GL41;
import spck.engine.debug.Stats;
import spck.engine.render.LayoutQualifier;
import spck.engine.render.MeshMaterialBatch;

public class OpenGLDefaultMaterialRenderer implements Renderer {
    @Override
    public void render(MeshMaterialBatch batch) {
        Stats.numOfVerts += batch.getMesh().getIndices().length;
        Stats.numOfTotalVerts += batch.getMesh().getIndices().length * batch.getNumOfEntities();

        GL.vaoContext(batch.getVaoID(), () -> {
            GL41.glEnableVertexAttribArray(LayoutQualifier.VX_POSITION.location);
            GL41.glEnableVertexAttribArray(LayoutQualifier.VX_NORMAL.location);

            if (batch.getMaterial().hasDiffuseTexture()) {
                GL41.glEnableVertexAttribArray(LayoutQualifier.VX_UV_COORDS.location);
                GL41.glEnableVertexAttribArray(LayoutQualifier.INS_UV_SCALE.location);
                GL41.glEnableVertexAttribArray(LayoutQualifier.INS_UV_OFFSET.location);
                GL41.glActiveTexture(batch.getMaterial().getDiffuseTexture().getGlTextureSlot());
                GL41.glBindTexture(GL41.GL_TEXTURE_2D, batch.getMaterial().getDiffuseTexture().getId());
            }

            // 4 slot for the transformation matrix, one for each column
            GL41.glEnableVertexAttribArray(LayoutQualifier.INS_TRANSFORMATION_MATRIX_COL1.location);
            GL41.glEnableVertexAttribArray(LayoutQualifier.INS_TRANSFORMATION_MATRIX_COL2.location);
            GL41.glEnableVertexAttribArray(LayoutQualifier.INS_TRANSFORMATION_MATRIX_COL3.location);
            GL41.glEnableVertexAttribArray(LayoutQualifier.INS_TRANSFORMATION_MATRIX_COL4.location);

            GL41.glDrawElementsInstanced(GL41.GL_TRIANGLES, batch.getMesh().getIndices().length, GL41.GL_UNSIGNED_INT, 0, batch.getNumOfEntities());

            GL41.glDisableVertexAttribArray(LayoutQualifier.VX_POSITION.location);
            GL41.glDisableVertexAttribArray(LayoutQualifier.VX_NORMAL.location);

            if (batch.getMaterial().hasDiffuseTexture()) {
                GL41.glDisableVertexAttribArray(LayoutQualifier.VX_UV_COORDS.location);
                GL41.glDisableVertexAttribArray(LayoutQualifier.INS_UV_SCALE.location);
                GL41.glDisableVertexAttribArray(LayoutQualifier.INS_UV_OFFSET.location);
                GL41.glBindTexture(GL41.GL_TEXTURE_2D, 0);
            }

            // 4 slot for the transformation matrix, one for each column
            GL41.glDisableVertexAttribArray(LayoutQualifier.INS_TRANSFORMATION_MATRIX_COL1.location);
            GL41.glDisableVertexAttribArray(LayoutQualifier.INS_TRANSFORMATION_MATRIX_COL2.location);
            GL41.glDisableVertexAttribArray(LayoutQualifier.INS_TRANSFORMATION_MATRIX_COL3.location);
            GL41.glDisableVertexAttribArray(LayoutQualifier.INS_TRANSFORMATION_MATRIX_COL4.location);
        });
    }
}
