package spck.engine.ecs.debug;

import spck.engine.ui.UIObjectPosition;

public class StatUIEntitiesBuilder {
    public void build() {
        new StatUITextEntity(StatUITextComponent.Type.FPS, "?", UIObjectPosition.topLeft(10, 10));
        new StatUITextEntity(StatUITextComponent.Type.VSYNC, "?", UIObjectPosition.topLeft(25, 10));
        new StatUITextEntity(StatUITextComponent.Type.RENDER_TIME, "?", UIObjectPosition.topLeft(40, 10));
        new StatUITextEntity(StatUITextComponent.Type.CAM_POS, "?", UIObjectPosition.topLeft(55, 10));
        new StatUITextEntity(StatUITextComponent.Type.CAM_ROT, "?", UIObjectPosition.topLeft(70, 10));
        new StatUITextEntity(StatUITextComponent.Type.VERTS, "?", UIObjectPosition.topLeft(85, 10));
        new StatUITextEntity(StatUITextComponent.Type.VERTS_TOTAL, "?", UIObjectPosition.topLeft(100, 10));
        new StatUITextEntity(StatUITextComponent.Type.BATCH_GROUPS, "?", UIObjectPosition.topLeft(115, 10));
        new StatUITextEntity(StatUITextComponent.Type.BATCHES, "?", UIObjectPosition.topLeft(130, 10));
        new StatUITextEntity(StatUITextComponent.Type.NUM_OF_ENTITIES, "?", UIObjectPosition.topLeft(145, 10));
        new StatUITextEntity(StatUITextComponent.Type.VBO_MEMORY_USED, "?", UIObjectPosition.topLeft(160, 10));
        new StatUITextEntity(StatUITextComponent.Type.JVM_MEMORY_FREE, "?", UIObjectPosition.topLeft(175, 10));
        new StatUITextEntity(StatUITextComponent.Type.JVM_MEMORY_ALLOCATED, "?", UIObjectPosition.topLeft(190, 10));
        new StatUITextEntity(StatUITextComponent.Type.JVM_MEMORY_MAX, "?", UIObjectPosition.topLeft(205, 10));
        new StatUITextEntity(StatUITextComponent.Type.JVM_MEMORY_TOTAL_FREE, "?", UIObjectPosition.topLeft(220, 10));
    }
}