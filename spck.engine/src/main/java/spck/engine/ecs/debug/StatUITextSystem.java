package spck.engine.ecs.debug;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import spck.engine.Engine;
import spck.engine.debug.Measure;
import spck.engine.debug.Stats;
import spck.engine.ecs.ui.UIText;
import spck.engine.render.camera.Camera;
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
                StatusType type = StatusType.valueOf(text.getId());
                switch (type) {
                    case FPS:
                        text.text("FPS: " + Measure.getLastFPS());
                        break;
                    case VSYNC:
                        text.text("Vsync: " + String.valueOf(Engine.window.getPreferences().isvSyncEnabled()).toUpperCase());
                        break;
                    case RENDER_TIME:
                        text.text(String.format("Render time: %.2fms, (GFX: %.2fms)", Measure.getLastRenderTime(), Measure.getLastGraphicsRenderTime()));
                        break;
                    case CAM_POS:
                        text.text(String.format("Cam pos: X:%.2f Y:%.2f Z:%.2f", camera.getPosition().x, camera.getPosition().y, camera.getPosition().z));
                        break;
                    case CAM_ROT:
                        text.text(String.format("Cam rot: X:%.2f Y:%.2f Z:%.2f", camera.getRotation().x, camera.getRotation().y, camera.getRotation().z));
                        break;
                    case VERTS:
                        text.text("Verts: " + NumberFormatter.formatSimple(Stats.numOfVerts));
                        break;
                    case VERTS_TOTAL:
                        text.text("Verts total: " + NumberFormatter.formatSimple(Stats.numOfTotalVerts));
                        break;
                    case BATCH_GROUPS:
                        text.text("Batch groups: " + NumberFormatter.formatSimple(Stats.numOfBatchGroups));
                        break;
                    case BATCHES:
                        text.text("Batches total: " + NumberFormatter.formatSimple(Stats.numOfBatches));
                        break;
                    case NUM_OF_ENTITIES:
                        text.text("Num of entities: " + NumberFormatter.formatSimple(Stats.numOfEntities));
                        break;
                    case VBO_MEMORY_USED:
                        text.text("VBO mem used: " + NumberFormatter.formatBinaryUnit(Stats.vboMemoryUsed) + (Stats.vboMemoryMisused ? " - !!! INVALID VBO-OFFSET USAGE !!!" : ""));
                        break;
                    case JVM_MEMORY_FREE:
                        Runtime r1 = Runtime.getRuntime();
                        text.text("JVM mem free: " + NumberFormatter.formatBinaryUnit(r1.freeMemory()));
                        break;
                    case JVM_MEMORY_ALLOCATED:
                        Runtime r2 = Runtime.getRuntime();
                        text.text("JVM mem allocated: " + NumberFormatter.formatBinaryUnit(r2.totalMemory()));
                        break;
                    case JVM_MEMORY_MAX:
                        Runtime r3 = Runtime.getRuntime();
                        text.text("JVM mem max: " + NumberFormatter.formatBinaryUnit(r3.maxMemory()));
                        break;
                    case JVM_MEMORY_TOTAL_FREE:
                        Runtime r4 = Runtime.getRuntime();
                        text.text("JVM mem total free: " + NumberFormatter.formatBinaryUnit(r4.freeMemory() + (r4.maxMemory() - r4.totalMemory())));
                        break;
                }
            }
        }
    }
}
