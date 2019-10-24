package spck.engine.ecs.debug;

import spck.engine.Align;
import spck.engine.ecs.ui.UICanvasEntity;
import spck.engine.ecs.ui.UIText;
import spck.engine.ui.UIObjectPosition;

public class StatusUICanvasEntity extends UICanvasEntity {
    @Override
    public void onEntityCreated() {
        canvasComponent = addComponent(StatusUICanvasComponent.class);
        canvasComponent.addText(UIText.build("?", UIObjectPosition.build(10, 10, Align.TOP_LEFT)).id(StatusType.FPS.name()));
        canvasComponent.addText(UIText.build("?", UIObjectPosition.build(10, 25, Align.TOP_LEFT)).id(StatusType.VSYNC.name()));
        canvasComponent.addText(UIText.build("?", UIObjectPosition.build(10, 40, Align.TOP_LEFT)).id(StatusType.RENDER_TIME.name()));
        canvasComponent.addText(UIText.build("?", UIObjectPosition.build(10, 55, Align.TOP_LEFT)).id(StatusType.CAM_POS.name()));
        canvasComponent.addText(UIText.build("?", UIObjectPosition.build(10, 70, Align.TOP_LEFT)).id(StatusType.CAM_ROT.name()));
        canvasComponent.addText(UIText.build("?", UIObjectPosition.build(10, 85, Align.TOP_LEFT)).id(StatusType.VERTS.name()));
        canvasComponent.addText(UIText.build("?", UIObjectPosition.build(10, 100, Align.TOP_LEFT)).id(StatusType.VERTS_TOTAL.name()));
        canvasComponent.addText(UIText.build("?", UIObjectPosition.build(10, 115, Align.TOP_LEFT)).id(StatusType.BATCH_GROUPS.name()));
        canvasComponent.addText(UIText.build("?", UIObjectPosition.build(10, 130, Align.TOP_LEFT)).id(StatusType.BATCHES.name()));
        canvasComponent.addText(UIText.build("?", UIObjectPosition.build(10, 145, Align.TOP_LEFT)).id(StatusType.NUM_OF_ENTITIES.name()));
        canvasComponent.addText(UIText.build("?", UIObjectPosition.build(10, 160, Align.TOP_LEFT)).id(StatusType.VBO_MEMORY_USED.name()));
        canvasComponent.addText(UIText.build("?", UIObjectPosition.build(10, 175, Align.TOP_LEFT)).id(StatusType.JVM_MEMORY_FREE.name()));
        canvasComponent.addText(UIText.build("?", UIObjectPosition.build(10, 190, Align.TOP_LEFT)).id(StatusType.JVM_MEMORY_ALLOCATED.name()));
        canvasComponent.addText(UIText.build("?", UIObjectPosition.build(10, 205, Align.TOP_LEFT)).id(StatusType.JVM_MEMORY_MAX.name()));
        canvasComponent.addText(UIText.build("?", UIObjectPosition.build(10, 220, Align.TOP_LEFT)).id(StatusType.JVM_MEMORY_TOTAL_FREE.name()));
    }
}
