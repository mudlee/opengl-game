package spck.engine.ecs.render;

import com.artemis.BaseSystem;
import spck.engine.Engine;
import spck.engine.debug.Stats;
import spck.engine.ecs.EntityBatchStore;
import spck.engine.render.MaterialBatchGroup;
import spck.engine.render.MeshMaterialBatch;
import spck.engine.render.PolygonShader;
import spck.engine.render.camera.Camera;
import spck.engine.util.RunOnce;

import java.util.HashSet;
import java.util.Set;

public class RenderSystem extends BaseSystem {
    private final EntityBatchStore batchStore;
    private final PolygonShader polygonShader;
    private final Set<Integer> entityIdsStat = new HashSet<>();

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
        entityIdsStat.clear();
        polygonShader.startShader(null);
        for (MaterialBatchGroup batchGroup : batchStore.getGroups().values()) {
            Stats.numOfBatchGroups++;
            Stats.numOfBatches += batchGroup.getBatches().size();
            batchGroup.getBatches().values().stream().map(MeshMaterialBatch::getEntityIDs).forEach(entityIdsStat::addAll);
        }

        Stats.numOfEntities += entityIdsStat.size();

        for (MaterialBatchGroup batchGroup : batchStore.getGroups().values()) {
            batchGroup.getBatches().values().forEach(batch -> batchGroup.getMaterial().getRenderer().render(batch));
        }
        polygonShader.stopShader();
    }

    private void forwardRender() {
        entityIdsStat.clear();
        for (MaterialBatchGroup batchGroup : batchStore.getGroups().values()) {
            Stats.numOfBatchGroups++;
            Stats.numOfBatches += batchGroup.getBatches().size();
            batchGroup.getBatches().values().stream().map(MeshMaterialBatch::getEntityIDs).forEach(entityIdsStat::addAll);
        }

        Stats.numOfEntities += entityIdsStat.size();

        for (MaterialBatchGroup batchGroup : batchStore.getGroups().values()) {
            batchGroup.getMaterial().getShader().startShader(batchGroup.getMaterial());
            batchGroup.getBatches().values().forEach(batch -> batchGroup.getMaterial().getRenderer().render(batch));
            batchGroup.getMaterial().getShader().stopShader();
        }
    }
}

