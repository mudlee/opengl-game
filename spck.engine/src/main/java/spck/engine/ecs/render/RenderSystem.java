package spck.engine.ecs.render;

import com.artemis.BaseSystem;
import spck.engine.Engine;
import spck.engine.debug.Stats;
import spck.engine.render.Camera;
import spck.engine.render.PolygonShader;
import spck.engine.util.RunOnce;

public class RenderSystem extends BaseSystem {
    private final PolygonShader polygonShader;
    private PreRenderSystem preRenderSystem;

    public RenderSystem(Camera camera) {
        polygonShader = new PolygonShader(camera);
    }

    @Override
    protected void processSystem() {
        RunOnce.run("PolygonShader init", polygonShader::init);

        preRenderSystem.getBatchStore().processChanges();

        if (Engine.preferences.polygonRenderMode) {
            polygonRender();
        } else {
            forwardRender();
        }
    }

    private void polygonRender() {
        polygonShader.startShader(null);
        preRenderSystem.getBatchStore().getGroups().forEach((groupId, batchGroup) -> {
            Stats.numOfBatchGroups++;
            Stats.numOfBatches += batchGroup.getBatches().size();
            batchGroup.getBatches().values().forEach(batch -> batchGroup.getMaterial().getRenderer().render(batch));
        });
        polygonShader.stopShader();
    }

    private void forwardRender() {
        preRenderSystem.getBatchStore().getGroups().forEach((groupId, batchGroup) -> {
            Stats.numOfBatchGroups++;
            Stats.numOfBatches += batchGroup.getBatches().size();

            batchGroup.getMaterial().getShader().startShader(batchGroup.getMaterial());
            batchGroup.getBatches().values().forEach(batch -> batchGroup.getMaterial().getRenderer().render(batch));
            batchGroup.getMaterial().getShader().stopShader();
        });
    }
}

