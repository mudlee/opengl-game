package spck.engine.ecs.render;

import com.artemis.BaseSystem;
import spck.engine.debug.Stats;
import spck.engine.ecs.EntityBatchStore;
import spck.engine.framework.*;
import spck.engine.render.MaterialBatchGroup;
import spck.engine.render.MeshMaterialBatch;
import spck.engine.render.camera.Camera;
import spck.engine.render.shader.AABBShader;
import spck.engine.render.shader.PolygonShader;
import spck.engine.render.shader.Shader;
import spck.engine.util.RunOnce;

import java.util.HashSet;
import java.util.Set;

public class RenderSystem extends BaseSystem {
    public static boolean polygonRenderMode;
    public static boolean renderAABB;
    private final MeshRenderer renderer;
    private final EntityBatchStore batchStore;
    private final PolygonShader polygonShader;
    private final AABBShader aabbShader;
    private final Set<Integer> entityIdsStat = new HashSet<>();
    private final MeshRenderer polygonOpenGLRenderer = new OpenGLPolygonRenderer();
    private final MeshRenderer aabbRenderer = new OpenGLAABBRenderer();
    private final Shader defaultShader;

    public RenderSystem(MeshRenderer renderer, EntityBatchStore batchStore, Camera camera) {
        this.renderer = renderer;
        this.batchStore = batchStore;
        polygonShader = new PolygonShader(camera);
        aabbShader = new AABBShader(camera);
        defaultShader = new OpenGLStandardShader(camera);
    }

    @Override
    protected void processSystem() {
        RunOnce.run("PolygonShader init", polygonShader::init);
        RunOnce.run("AABBShader init", aabbShader::init);
        RunOnce.run("Default shader init", defaultShader::init);

        batchStore.processChanges();

        if (polygonRenderMode) {
            polygonRender();
        } else {
            forwardRender();
        }

        if (renderAABB) {
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
            batchGroup.getMaterial().getShader().orElse(defaultShader).startShader(batchGroup.getMaterial());
            for (MeshMaterialBatch meshMaterialBatch : batchGroup.getBatches().values()) {
                renderer.render(meshMaterialBatch);
            }
            batchGroup.getMaterial().getShader().orElse(defaultShader).stopShader();
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

