package spck.engine.ecs.debug;

import org.joml.Vector2f;
import spck.engine.ecs.Entity;
import spck.engine.ecs.ui.UICanvasScaler;
import spck.engine.ecs.ui.UIText;
import spck.engine.ui.UIObjectPosition;

public class StatUIEntitiesBuilder {
    public void build() {
        UICanvasScaler scaler = UICanvasScaler.scaleWithPixel(new Vector2f(1024, 768), 0.5f);
        StatusUICanvasEntity canvas = new StatusUICanvasEntity(scaler);
        Entity.create(canvas);

        canvas.addText(UIText.build("?", UIObjectPosition.topLeft(10, 10)).id(StatusType.FPS.name()));
        canvas.addText(UIText.build("?", UIObjectPosition.topLeft(25, 10)).id(StatusType.VSYNC.name()));
        canvas.addText(UIText.build("?", UIObjectPosition.topLeft(40, 10)).id(StatusType.RENDER_TIME.name()));
        canvas.addText(UIText.build("?", UIObjectPosition.topLeft(55, 10)).id(StatusType.CAM_POS.name()));
        canvas.addText(UIText.build("?", UIObjectPosition.topLeft(70, 10)).id(StatusType.CAM_ROT.name()));
        canvas.addText(UIText.build("?", UIObjectPosition.topLeft(85, 10)).id(StatusType.VERTS.name()));
        canvas.addText(UIText.build("?", UIObjectPosition.topLeft(100, 10)).id(StatusType.VERTS_TOTAL.name()));
        canvas.addText(UIText.build("?", UIObjectPosition.topLeft(115, 10)).id(StatusType.BATCH_GROUPS.name()));
        canvas.addText(UIText.build("?", UIObjectPosition.topLeft(130, 10)).id(StatusType.BATCHES.name()));
        canvas.addText(UIText.build("?", UIObjectPosition.topLeft(145, 10)).id(StatusType.NUM_OF_ENTITIES.name()));
        canvas.addText(UIText.build("?", UIObjectPosition.topLeft(160, 10)).id(StatusType.VBO_MEMORY_USED.name()));
        canvas.addText(UIText.build("?", UIObjectPosition.topLeft(175, 10)).id(StatusType.JVM_MEMORY_FREE.name()));
        canvas.addText(UIText.build("?", UIObjectPosition.topLeft(190, 10)).id(StatusType.JVM_MEMORY_ALLOCATED.name()));
        canvas.addText(UIText.build("?", UIObjectPosition.topLeft(205, 10)).id(StatusType.JVM_MEMORY_MAX.name()));
        canvas.addText(UIText.build("?", UIObjectPosition.topLeft(220, 10)).id(StatusType.JVM_MEMORY_TOTAL_FREE.name()));
    }
}
