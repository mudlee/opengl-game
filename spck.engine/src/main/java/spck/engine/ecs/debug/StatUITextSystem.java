package spck.engine.ecs.debug;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import spck.engine.Engine;
import spck.engine.debug.Measure;
import spck.engine.debug.Stats;
import spck.engine.ecs.ui.UITextComponent;
import spck.engine.render.Camera;
import spck.engine.util.NumberFormatter;

public class StatUITextSystem extends IteratingSystem {
    private final Camera camera;
    private ComponentMapper<UITextComponent> textComponents;
    private ComponentMapper<StatUITextComponent> statComponents;

    public StatUITextSystem(Camera camera) {
        super(Aspect.all(StatUITextComponent.class));
        this.camera = camera;
    }

    @Override
    protected void process(int entityId) {
        UITextComponent textComponent = textComponents.get(entityId);
        StatUITextComponent statComponent = statComponents.get(entityId);

        switch (statComponent.type) {
            case FPS:
                textComponent.text = "FPS: " + String.valueOf(Measure.getLastFPS());
                break;
            case VSYNC:
                textComponent.text = "Vsync: " + String.valueOf(Engine.window.getPreferences().isvSyncEnabled()).toUpperCase();
                break;
            case RENDER_TIME:
                textComponent.text = String.format("Render time: %.2fms, (GFX: %.2fms)", Measure.getLastRenderTime(), Measure.getLastGraphicsRenderTime());
                break;
            case CAM_POS:
                textComponent.text = String.format("Cam pos: X:%.2f Y:%.2f Z:%.2f", camera.getPosition().x, camera.getPosition().y, camera.getPosition().z);
                break;
            case CAM_ROT:
                textComponent.text = String.format("Cam rot: X:%.2f Y:%.2f Z:%.2f", camera.getRotation().x, camera.getRotation().y, camera.getRotation().z);
                break;
            case VERTS:
                textComponent.text = "Verts: " + NumberFormatter.formatSimple(Stats.numOfVerts);
                break;
            case VERTS_TOTAL:
                textComponent.text = "Verts total: " + NumberFormatter.formatSimple(Stats.numOfTotalVerts);
                break;
            case BATCH_GROUPS:
                textComponent.text = "Batch groups: " + NumberFormatter.formatSimple(Stats.numOfBatchGroups);
                break;
            case BATCHES:
                textComponent.text = "Batches total: " + NumberFormatter.formatSimple(Stats.numOfBatches);
                break;
            case NUM_OF_ENTITIES:
                textComponent.text = "Num of entities: " + NumberFormatter.formatSimple(Stats.numOfEntities);
                break;
            case VBO_MEMORY_USED:
                textComponent.text = "VBO memory used: " + NumberFormatter.formatBinaryUnit(Stats.vboMemoryUsed) + (Stats.vboMemoryMisused ? " - !!! INVALID VBO-OFFSET USAGE !!!" : "");
                break;
        }
    }
}
