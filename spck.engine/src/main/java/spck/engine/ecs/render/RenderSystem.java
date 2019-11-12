package spck.engine.ecs.render;

import com.artemis.BaseSystem;
import spck.engine.Engine;
import spck.engine.debug.Stats;
import spck.engine.ecs.EntityBatchStore;
import spck.engine.framework.Graphics;
import spck.engine.framework.OpenGLAABBRenderer;
import spck.engine.framework.OpenGLPolygonRenderer;
import spck.engine.framework.Renderer;
import spck.engine.render.MaterialBatchGroup;
import spck.engine.render.MeshMaterialBatch;
import spck.engine.render.camera.Camera;
import spck.engine.render.shader.AABBShader;
import spck.engine.render.shader.PolygonShader;
import spck.engine.util.RunOnce;

import java.util.HashSet;
import java.util.Set;

public class RenderSystem extends BaseSystem {
    private final EntityBatchStore batchStore;
    private final PolygonShader polygonShader;
    private final AABBShader aabbShader;
    private final Set<Integer> entityIdsStat = new HashSet<>();
    private final Renderer polygonOpenGLRenderer = new OpenGLPolygonRenderer();
    private final Renderer aabbRenderer = new OpenGLAABBRenderer();

    public RenderSystem(EntityBatchStore batchStore, Camera camera) {
        this.batchStore = batchStore;
        polygonShader = new PolygonShader(camera);
        aabbShader = new AABBShader(camera);
    }

    @Override
    protected void processSystem() {
        RunOnce.run("PolygonShader init", polygonShader::init);
        RunOnce.run("AABBShader init", aabbShader::init);

        batchStore.processChanges();

        if (Engine.preferences.polygonRenderMode) {
            polygonRender();
        } else {
            forwardRender();
        }

        if (Engine.preferences.renderAABB) {
            aabbRender();
        }
    }

    private void aabbRender() {
        Graphics.setPolygonMode(Graphics.PolygonMode.LINE);
        aabbShader.startShader(null);
        for (MaterialBatchGroup batchGroup : batchStore.getGroups().values()) {
            for (MeshMaterialBatch meshMaterialBatch : batchGroup.getBatches().values()) {
                aabbRenderer.render(meshMaterialBatch);
            }
        }
        aabbShader.stopShader();
        Graphics.setPolygonMode(Graphics.PolygonMode.FILL);
    }

    private void polygonRender() {
        Graphics.setPolygonMode(Graphics.PolygonMode.LINE);
        updateStats();
        polygonShader.startShader(null);
        for (MaterialBatchGroup batchGroup : batchStore.getGroups().values()) {
            for (MeshMaterialBatch meshMaterialBatch : batchGroup.getBatches().values()) {
                polygonOpenGLRenderer.render(meshMaterialBatch);
            }
        }
        polygonShader.stopShader();
        Graphics.setPolygonMode(Graphics.PolygonMode.FILL);
    }

    private void forwardRender() {
        updateStats();

        for (MaterialBatchGroup batchGroup : batchStore.getGroups().values()) {
            batchGroup.getMaterial().getShader().startShader(batchGroup.getMaterial());
            for (MeshMaterialBatch meshMaterialBatch : batchGroup.getBatches().values()) {
                Engine.renderer.render(meshMaterialBatch);
            }
            batchGroup.getMaterial().getShader().stopShader();
        }
    }

    private void updateStats() {
        entityIdsStat.clear();
        for (MaterialBatchGroup batchGroup : batchStore.getGroups().values()) {
            Stats.numOfBatchGroups++;
            Stats.numOfBatches += batchGroup.getBatches().size();
            for (MeshMaterialBatch meshMaterialBatch : batchGroup.getBatches().values()) {
                entityIdsStat.addAll(meshMaterialBatch.getEntityIDs());
            }
        }
        Stats.numOfEntities += entityIdsStat.size();
    }
}

