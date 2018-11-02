package spck.engine.ecs.render;

import com.artemis.BaseSystem;
import spck.engine.Engine;
import spck.engine.debug.Stats;
import spck.engine.ecs.EntityBatchStore;
import spck.engine.render.Batch;
import spck.engine.render.Camera;
import spck.engine.render.PolygonShader;
import spck.engine.util.RunOnce;

public class RenderSystem extends BaseSystem {
    private final EntityBatchStore batchStore;
    private final PolygonShader polygonShader;

    public RenderSystem(EntityBatchStore batchStore, Camera camera) {
        this.batchStore = batchStore;
        polygonShader = new PolygonShader(camera);
    }

    @Override
    protected void processSystem() {
        RunOnce.run("PolygonShader init", polygonShader::init);

        batchStore.processChanges();

        if (Engine.preferences.polygonRenderMode) {
            polygonRender();
        } else {
            forwardRender();
        }
    }

    private void polygonRender() {
        polygonShader.startShader(null);
        batchStore.getGroups().forEach((groupId, batchGroup) -> {
            Stats.numOfBatchGroups++;
            Stats.numOfBatches += batchGroup.getBatches().size();
            Stats.numOfEntities += batchGroup.getBatches().values().stream().mapToInt(Batch::getNumOfEntities).sum();
            batchGroup.getBatches().values().forEach(batch -> batchGroup.getMaterial().getRenderer().render(batch));
        });
        polygonShader.stopShader();
    }

    private void forwardRender() {
        batchStore.getGroups().forEach((groupId, batchGroup) -> {
            Stats.numOfBatchGroups++;
            Stats.numOfBatches += batchGroup.getBatches().size();
            Stats.numOfEntities += batchGroup.getBatches().values().stream().mapToInt(Batch::getNumOfEntities).sum();

            batchGroup.getMaterial().getShader().startShader(batchGroup.getMaterial());
            batchGroup.getBatches().values().forEach(batch -> batchGroup.getMaterial().getRenderer().render(batch));
            batchGroup.getMaterial().getShader().stopShader();
        });
    }
}

