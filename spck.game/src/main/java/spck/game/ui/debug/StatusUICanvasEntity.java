package spck.game.ui.debug;

import spck.engine.Align;
import spck.engine.ecs.ui.UICanvasEntity;
import spck.engine.ecs.ui.UIText;
import spck.engine.framework.RGBAColor;
import spck.engine.ui.UIObjectPosition;

public class StatusUICanvasEntity extends UICanvasEntity {
    @Override
    public void onEntityCreated() {
        canvasComponent = addComponent(StatusUICanvasComponent.class);
        canvasComponent.addText(UIText.build("?", UIObjectPosition.build(10, 10, Align.TOP_LEFT)).id(StatusType.FPS.name()).color(RGBAColor.black()));
        canvasComponent.addText(UIText.build("?", UIObjectPosition.build(10, 45, Align.TOP_LEFT)).id(StatusType.VSYNC.name()).color(RGBAColor.black()));
        canvasComponent.addText(UIText.build("?", UIObjectPosition.build(10, 80, Align.TOP_LEFT)).id(StatusType.RENDER_TIME.name()).color(RGBAColor.black()));
        canvasComponent.addText(UIText.build("?", UIObjectPosition.build(10, 115, Align.TOP_LEFT)).id(StatusType.CAM_POS.name()).color(RGBAColor.black()));
        canvasComponent.addText(UIText.build("?", UIObjectPosition.build(10, 150, Align.TOP_LEFT)).id(StatusType.CAM_ROT.name()).color(RGBAColor.black()));
        canvasComponent.addText(UIText.build("?", UIObjectPosition.build(10, 185, Align.TOP_LEFT)).id(StatusType.CAM_SIZE.name()).color(RGBAColor.black()));
        canvasComponent.addText(UIText.build("?", UIObjectPosition.build(10, 220, Align.TOP_LEFT)).id(StatusType.VERTS.name()).color(RGBAColor.black()));
        canvasComponent.addText(UIText.build("?", UIObjectPosition.build(10, 255, Align.TOP_LEFT)).id(StatusType.VERTS_TOTAL.name()).color(RGBAColor.black()));
        canvasComponent.addText(UIText.build("?", UIObjectPosition.build(10, 290, Align.TOP_LEFT)).id(StatusType.BATCH_GROUPS.name()).color(RGBAColor.black()));
        canvasComponent.addText(UIText.build("?", UIObjectPosition.build(10, 325, Align.TOP_LEFT)).id(StatusType.BATCHES.name()).color(RGBAColor.black()));
        canvasComponent.addText(UIText.build("?", UIObjectPosition.build(10, 360, Align.TOP_LEFT)).id(StatusType.NUM_OF_ENTITIES.name()).color(RGBAColor.black()));
        canvasComponent.addText(UIText.build("?", UIObjectPosition.build(10, 395, Align.TOP_LEFT)).id(StatusType.VBO_MEMORY_USED.name()).color(RGBAColor.black()));
        canvasComponent.addText(UIText.build("?", UIObjectPosition.build(10, 430, Align.TOP_LEFT)).id(StatusType.JVM_MEMORY_FREE.name()).color(RGBAColor.black()));
        canvasComponent.addText(UIText.build("?", UIObjectPosition.build(10, 465, Align.TOP_LEFT)).id(StatusType.JVM_MEMORY_ALLOCATED.name()).color(RGBAColor.black()));
        canvasComponent.addText(UIText.build("?", UIObjectPosition.build(10, 500, Align.TOP_LEFT)).id(StatusType.JVM_MEMORY_MAX.name()).color(RGBAColor.black()));
        canvasComponent.addText(UIText.build("?", UIObjectPosition.build(10, 535, Align.TOP_LEFT)).id(StatusType.JVM_MEMORY_TOTAL_FREE.name()).color(RGBAColor.black()));
        canvasComponent.addText(UIText.build("?", UIObjectPosition.build(10, 570, Align.TOP_LEFT)).id(StatusType.AABB_RENDERING.name()).color(RGBAColor.black()));
    }
}
