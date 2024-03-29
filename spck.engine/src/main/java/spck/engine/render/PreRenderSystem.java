package spck.engine.render;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spck.engine.ecs.ComponentState;
import spck.engine.render.shader.Shader;

import java.util.List;

public class PreRenderSystem extends IteratingSystem {
    private static final Logger log = LoggerFactory.getLogger(PreRenderSystem.class);
	private final Runnable doNothing = () -> {
	};
	private final RenderBatchStore batchStore;
	private ComponentMapper<RenderComponent> meshRendererMapper;

	public PreRenderSystem(RenderBatchStore batchStore) {
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

        if (batchStore.containsEntity(entityId)) {
            List<MeshMaterialPair> meshChanges = component.meshMaterialCollection.ackMeshChanges();
            List<MeshMaterialPair> materialChanges = component.meshMaterialCollection.ackMaterialChanges();

            for (MeshMaterialPair materialChange : materialChanges) {
                batchStore.entityMaterialHasChanged(entityId, materialChange);
            }

            for (MeshMaterialPair meshChange : meshChanges) {
                batchStore.entityMeshHasChanged(entityId, meshChange);
            }

            component.transform.processChanges(() -> batchStore.entityTransformHasChanged(entityId, component));
        } else {
			component.meshMaterialCollection.getCollection().forEach(meshMaterialPair -> batchStore.add(entityId, meshMaterialPair));
			for (MeshMaterialPair meshMaterialPair : component.meshMaterialCollection.getCollection()) {
				meshMaterialPair.getMaterial().getShader().ifPresent(this::initShaderIfNeeded);
			}

			// mark everything up2date as the data was just uploaded to the GPU
			component.meshMaterialCollection.ackMeshChanges();
			component.meshMaterialCollection.ackMaterialChanges();
			component.transform.processChanges(doNothing);
		}
	}

	private void initShaderIfNeeded(Shader shader) {
		if (!shader.isInitialized()) {
			log.debug("Initialising shader {}", shader);
			shader.init();
			log.debug("Shader {} initialised", shader);
		}
	}
}
