package spck.engine.ecs.debug;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import spck.engine.Engine;
import spck.engine.debug.Measure;
import spck.engine.debug.Stats;
import spck.engine.ecs.ui.UIText;
import spck.engine.render.Camera;
import spck.engine.util.NumberFormatter;

public class StatUITextSystem extends IteratingSystem {
    private final Camera camera;
    private ComponentMapper<StatusUICanvasComponent> canvasComponents;

    public StatUITextSystem(Camera camera) {
        super(Aspect.all(StatusUICanvasComponent.class));
        this.camera = camera;
    }

    @Override
    protected void process(int entityId) {
        if (canvasComponents.has(entityId)) {
            StatusUICanvasComponent canvas = canvasComponents.get(entityId);
            for (UIText text : canvas.getTexts()) {
                StatusType type = StatusType.valueOf(text.getCustomID());
                switch (type) {
                    case FPS:
                        text.setText("FPS: " + String.valueOf(Measure.getLastFPS()));
                        break;
                    case VSYNC:
                        text.setText("Vsync: " + String.valueOf(Engine.window.getPreferences().isvSyncEnabled()).toUpperCase());
                        break;
                    case RENDER_TIME:
                        text.setText(String.format("Render time: %.2fms, (GFX: %.2fms)", Measure.getLastRenderTime(), Measure.getLastGraphicsRenderTime()));
                        break;
                    case CAM_POS:
                        text.setText(String.format("Cam pos: X:%.2f Y:%.2f Z:%.2f", camera.getPosition().x, camera.getPosition().y, camera.getPosition().z));
                        break;
                    case CAM_ROT:
                        text.setText(String.format("Cam rot: X:%.2f Y:%.2f Z:%.2f", camera.getRotation().x, camera.getRotation().y, camera.getRotation().z));
                        break;
                    case VERTS:
                        text.setText("Verts: " + NumberFormatter.formatSimple(Stats.numOfVerts));
                        break;
                    case VERTS_TOTAL:
                        text.setText("Verts total: " + NumberFormatter.formatSimple(Stats.numOfTotalVerts));
                        break;
                    case BATCH_GROUPS:
                        text.setText("Batch groups: " + NumberFormatter.formatSimple(Stats.numOfBatchGroups));
                        break;
                    case BATCHES:
                        text.setText("Batches total: " + NumberFormatter.formatSimple(Stats.numOfBatches));
                        break;
                    case NUM_OF_ENTITIES:
                        text.setText("Num of entities: " + NumberFormatter.formatSimple(Stats.numOfEntities));
                        break;
                    case VBO_MEMORY_USED:
                        text.setText("VBO mem used: " + NumberFormatter.formatBinaryUnit(Stats.vboMemoryUsed) + (Stats.vboMemoryMisused ? " - !!! INVALID VBO-OFFSET USAGE !!!" : ""));
                        break;
                    case JVM_MEMORY_FREE:
                        Runtime r1 = Runtime.getRuntime();
                        text.setText("JVM mem free: " + NumberFormatter.formatBinaryUnit(r1.freeMemory()));
                        break;
                    case JVM_MEMORY_ALLOCATED:
                        Runtime r2 = Runtime.getRuntime();
                        text.setText("JVM mem allocated: " + NumberFormatter.formatBinaryUnit(r2.totalMemory()));
                        break;
                    case JVM_MEMORY_MAX:
                        Runtime r3 = Runtime.getRuntime();
                        text.setText("JVM mem max: " + NumberFormatter.formatBinaryUnit(r3.maxMemory()));
                        break;
                    case JVM_MEMORY_TOTAL_FREE:
                        Runtime r4 = Runtime.getRuntime();
                        text.setText("JVM mem total free: " + NumberFormatter.formatBinaryUnit(r4.freeMemory() + (r4.maxMemory() - r4.totalMemory())));
                        break;
                }
            }
        }
    }
}
