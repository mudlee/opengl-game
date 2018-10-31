package spck.engine.ecs.render;

// TODO: when entity is removed, unload its data

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spck.engine.ecs.EntityBatchStore;
import spck.engine.ecs.render.components.RenderComponent;
import spck.engine.render.Shader;

import java.util.ArrayList;
import java.util.List;

public class PreRenderSystem extends IteratingSystem {
    private static final Logger LOGGER = LoggerFactory.getLogger(PreRenderSystem.class);
    private final Runnable doNothing = () -> {
    };
    private final EntityBatchStore batchStore = new EntityBatchStore();
    private final List<Shader> initialisedShaders = new ArrayList<>();
    private ComponentMapper<RenderComponent> meshRendererMapper;

    public PreRenderSystem() {
        super(Aspect.all(RenderComponent.class));
    }

    @Override
    protected void process(int entityId) {
        RenderComponent component = meshRendererMapper.get(entityId);

        if (batchStore.contains(entityId)) {
            component.material.ackAndComputeChanged(() -> batchStore.entityMaterialHasChanged(entityId, component));
            component.mesh.ackAndComputeChanged(() -> batchStore.entityMeshHasChanged(entityId, component));
            component.transform.ackAndComputeChanged(() -> batchStore.entityTransformHasChanged(entityId, component));
        } else {
            batchStore.add(entityId, component);
            initShaderIfNeeded(component.material.getShader());

            // mark everything up2date as the data was just uploaded to the GPU
            component.material.ackAndComputeChanged(doNothing);
            component.mesh.ackAndComputeChanged(doNothing);
            component.transform.ackAndComputeChanged(doNothing);
        }
    }

    EntityBatchStore getBatchStore() {
        return batchStore;
    }

    private void initShaderIfNeeded(Shader shader) {
        if (!initialisedShaders.contains(shader)) {
            LOGGER.debug("Initialising shader {}", shader);
            shader.init();
            initialisedShaders.add(shader);
            LOGGER.debug("Shader {} initialised", shader);
        }
    }
}
