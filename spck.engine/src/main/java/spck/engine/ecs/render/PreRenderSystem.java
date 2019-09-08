package spck.engine.ecs.render;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spck.engine.ecs.ComponentState;
import spck.engine.ecs.EntityBatchStore;
import spck.engine.ecs.render.components.RenderComponent;
import spck.engine.render.Shader;

import java.util.ArrayList;
import java.util.List;

public class PreRenderSystem extends IteratingSystem {
    private static final Logger LOGGER = LoggerFactory.getLogger(PreRenderSystem.class);
    private final Runnable doNothing = () -> {
    };
    private final EntityBatchStore batchStore;
    private final List<Shader> initialisedShaders = new ArrayList<>();
    private ComponentMapper<RenderComponent> meshRendererMapper;

    public PreRenderSystem(EntityBatchStore batchStore) {
        super(Aspect.all(RenderComponent.class));
        this.batchStore = batchStore;
    }

    @Override
    protected void process(int entityId) {
        RenderComponent component = meshRendererMapper.get(entityId);

        if (component.state == ComponentState.MARKED_FOR_DESTROY) {
            batchStore.destroyEntity(entityId);
            component.state = ComponentState.DESTROYED;
            return;
        }

        if (batchStore.contains(entityId)) {
            component.material.processChanges(() -> batchStore.entityMaterialHasChanged(entityId, component));
            component.mesh.processChanges(() -> batchStore.entityMeshHasChanged(entityId, component));
            component.transform.processChanges(() -> batchStore.entityTransformHasChanged(entityId, component));
        } else {
            batchStore.add(entityId, component);
            initShaderIfNeeded(component.material.getShader());

            // mark everything up2date as the data was just uploaded to the GPU
            component.material.processChanges(doNothing);
            component.mesh.processChanges(doNothing);
            component.transform.processChanges(doNothing);
        }
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
