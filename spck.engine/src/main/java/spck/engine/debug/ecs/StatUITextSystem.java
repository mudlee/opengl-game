package spck.engine.debug.ecs;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import spck.engine.Engine;
import spck.engine.debug.Measure;
import spck.engine.debug.Stats;
import spck.engine.ui.ecs.UITextComponent;
import spck.engine.util.NumberFormatter;

public class StatUITextSystem extends IteratingSystem {
    //private final Camera camera;
    private ComponentMapper<UITextComponent> textComponents;
    private ComponentMapper<StatUITextComponent> statComponents;

    public StatUITextSystem() {
        super(Aspect.all(StatUITextComponent.class));
        //this.camera = camera;
    }

    @Override
    protected void process(int entityId) {
        UITextComponent textComponent = textComponents.get(entityId);
        StatUITextComponent statComponent = statComponents.get(entityId);

        switch (statComponent.type) {
            case FPS:
                //textComponent.text = "FPS: " + String.valueOf(Measure.getFPS());
                textComponent.text = "FPS: ";
                break;
            case VSYNC:
                textComponent.text = "Vsync: " + String.valueOf(Engine.window.getPreferences().isvSyncEnabled()).toUpperCase();
                break;
            case RENDER_TIME:
                textComponent.text = String.format("Render time: %.2fms, (GFX: %.2fms)", Measure.getLastRenderTime(), Measure.getLastGraphicsRenderTime());
                break;
            case CAM_POS:
                //textComponent.text = String.format("Cam pos: X:%.2f Y:%.2f Z:%.2f", camera.getPosition().x, camera.getPosition().y, camera.getPosition().z);
                break;
            case CAM_ROT:
                //textComponent.text = String.format("Cam rot: X:%.2f Y:%.2f Z:%.2f", camera.getRotation().x, camera.getRotation().y, camera.getRotation().z);
                break;
            case VERTS:
                textComponent.text = "Verts: " + NumberFormatter.format(Stats.numOfVerts);
                break;
            case VERTS_TOTAL:
                textComponent.text = "Verts total: " + NumberFormatter.format(Stats.numOfTotalVerts);
                break;
            case BATCH_GROUPS:
                textComponent.text = "Batch groups: " + NumberFormatter.format(Stats.numOfBatchGroups);
                break;
            case BATCHES:
                textComponent.text = "Batches total: " + NumberFormatter.format(Stats.numOfBatches);
                break;
        }
    }
}
