package spck.engine.ecs;

import org.assertj.core.api.Assertions;
import org.joml.Vector3f;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import spck.engine.Engine;
import spck.engine.ecs.render.components.RenderComponent;
import spck.engine.framework.OpenGLStandardRenderer;
import spck.engine.render.Batch;
import spck.engine.render.BatchGroup;
import spck.engine.render.DefaultMaterial;
import spck.engine.render.Material;
import spck.engine.render.Mesh;
import spck.engine.render.Transform;

import java.util.ArrayList;
import java.util.List;

class EntityBatchStoreTest {
    private EntityBatchStore store;

    @BeforeEach
    void before() {
        store = new EntityBatchStore();
    }

    @Test
    void addingSingle() {
        store.add(1, createMeshRendererComponent(createEmptyMesh(), new DefaultMaterial()));

        Assertions.assertThat(store.getGroups().size()).isEqualTo(1);
        Assertions.assertThat(store.getGroups().values().stream().findFirst().isPresent()).isEqualTo(true);
        BatchGroup group = store.getGroups().values().stream().findFirst().get();
        Assertions.assertThat(group.getBatches().size()).isEqualTo(1);
        Assertions.assertThat(group.getBatches().values().stream().findFirst().isPresent()).isEqualTo(true);
        Assertions.assertThat(group.getBatches().values().stream().findFirst().get().getEntities().size()).isEqualTo(1);
        Assertions.assertThat(group.getBatches().values().stream().findFirst().get().getEntities().contains(1)).isTrue();
    }

    @Test
    void addSingleMultipleTimes() {
        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> {
            store.add(1, createMeshRendererComponent(createEmptyMesh(), new DefaultMaterial()));
            store.add(1, createMeshRendererComponent(createEmptyMesh(), new DefaultMaterial()));
        });
    }

    @Test
    void addingMultipleSameMatSameMesh() {
        store.add(1, createMeshRendererComponent(createEmptyMesh(), new DefaultMaterial()));
        store.add(2, createMeshRendererComponent(createEmptyMesh(), new DefaultMaterial()));

        Assertions.assertThat(store.getGroups().size()).isEqualTo(1);

        Assertions.assertThat(store.getGroups().values().stream().findFirst().isPresent()).isEqualTo(true);
        BatchGroup group = store.getGroups().values().stream().findFirst().get();
        Assertions.assertThat(group.getBatches().size()).isEqualTo(1);
        Assertions.assertThat(group.getBatches().values().stream().findFirst().isPresent()).isEqualTo(true);
        Assertions.assertThat(group.getBatches().values().stream().findFirst().get().getEntities().size()).isEqualTo(2);
        Assertions.assertThat(group.getBatches().values().stream().findFirst().get().getEntities().contains(1)).isTrue();
        Assertions.assertThat(group.getBatches().values().stream().findFirst().get().getEntities().contains(2)).isTrue();
    }

    @Test
    void addingMultipleSameMatDiffMesh() {
        Material mat = new DefaultMaterial();

        store.add(1, createMeshRendererComponent(createEmptyMesh(), mat));
        store.add(2, createMeshRendererComponent(createEmptyMesh(), mat));

        List<BatchGroup> batchGroups = new ArrayList<>(store.getGroups().values());
        Assertions.assertThat(batchGroups.size()).isEqualTo(1);

        Assertions.assertThat(batchGroups.get(0).getBatches().size()).isEqualTo(1);
        List<Batch> groupBatches = new ArrayList<>(batchGroups.get(0).getBatches().values());
        Assertions.assertThat(groupBatches.get(0).getEntities().size()).isEqualTo(2);
    }

    @Test
    void addingMultipleDiffMatSameMesh() {
        Material mat1 = new DefaultMaterial();
        Material mat2 = new DefaultMaterial();
        Material mat3 = new DefaultMaterial();
        mat2.setDiffuseColor(new Vector3f(0, 0, 0));
        mat3.setDiffuseColor(new Vector3f(0, 0, 0));

        // NOTE: mat2 and mat3 should be treat as same
        store.add(1, createMeshRendererComponent(createEmptyMesh(), mat1));
        store.add(2, createMeshRendererComponent(createEmptyMesh(), mat2));
        store.add(3, createMeshRendererComponent(createEmptyMesh(), mat2));
        store.add(4, createMeshRendererComponent(createEmptyMesh(), mat3));

        List<BatchGroup> batchGroups = new ArrayList<>(store.getGroups().values());
        Assertions.assertThat(batchGroups.size()).isEqualTo(2);

        Assertions.assertThat(batchGroups.get(0).getBatches().size()).isEqualTo(1);
        Assertions.assertThat(batchGroups.get(1).getBatches().size()).isEqualTo(1);

        List<Batch> group1Batches = new ArrayList<>(batchGroups.get(0).getBatches().values());
        List<Batch> group2Batches = new ArrayList<>(batchGroups.get(1).getBatches().values());
        Assertions.assertThat(group1Batches.size()).isEqualTo(1);
        Assertions.assertThat(group2Batches.size()).isEqualTo(1);

        Assertions.assertThat(group1Batches.get(0).getEntities().size()).isEqualTo(1);
        Assertions.assertThat(group2Batches.get(0).getEntities().size()).isEqualTo(3);

        Assertions.assertThat(group1Batches.get(0).getEntities().contains(1)).isTrue();

        Assertions.assertThat(group2Batches.get(0).getEntities().contains(2)).isTrue();
        Assertions.assertThat(group2Batches.get(0).getEntities().contains(3)).isTrue();
        Assertions.assertThat(group2Batches.get(0).getEntities().contains(4)).isTrue();
    }

    @Test
    void addingMultipleDiffMatDiffMesh() {
        mockStandardRenderer();

        Material mat1 = new DefaultMaterial();
        Material mat2 = new DefaultMaterial();
        mat2.setDiffuseColor(new Vector3f(0, 0, 0));

        Mesh mesh1 = createEmptyMesh();
        Mesh mesh2 = createEmptyMesh();
        mesh2.setIndices(new int[]{1, 2, 3});

        store.add(1, createMeshRendererComponent(mesh1, mat1));
        store.add(2, createMeshRendererComponent(mesh2, mat2));

        List<BatchGroup> batchGroups = new ArrayList<>(store.getGroups().values());
        Assertions.assertThat(batchGroups.size()).isEqualTo(2);

        Assertions.assertThat(batchGroups.get(0).getBatches().size()).isEqualTo(1);
        Assertions.assertThat(batchGroups.get(1).getBatches().size()).isEqualTo(1);
        List<Batch> group1Batches = new ArrayList<>(batchGroups.get(0).getBatches().values());
        Assertions.assertThat(group1Batches.get(0).getEntities().size()).isEqualTo(1);
        List<Batch> group2Batches = new ArrayList<>(batchGroups.get(1).getBatches().values());
        Assertions.assertThat(group2Batches.get(0).getEntities().size()).isEqualTo(1);
    }

    @Test
    void changeMaterial() {
        mockStandardRenderer();

        Material mat1 = new DefaultMaterial();
        Material mat2 = new DefaultMaterial();

        RenderComponent comp2 = createMeshRendererComponent(createEmptyMesh(), mat2);

        store.add(1, createMeshRendererComponent(createEmptyMesh(), mat1));
        store.add(2, comp2);

        List<BatchGroup> batchGroups = new ArrayList<>(store.getGroups().values());
        Assertions.assertThat(batchGroups.size()).isEqualTo(1);

        Assertions.assertThat(batchGroups.get(0).getBatches().size()).isEqualTo(1);
        List<Batch> groupBatches = new ArrayList<>(batchGroups.get(0).getBatches().values());
        Assertions.assertThat(groupBatches.get(0).getEntities().size()).isEqualTo(2);

        // change obj2's material
        comp2.material.setDiffuseColor(new Vector3f(0, 0, 0));
        store.entityMaterialHasChanged(2, comp2);
        store.processChanges();

        List<BatchGroup> batchGroupsAfter = new ArrayList<>(store.getGroups().values());
        Assertions.assertThat(batchGroupsAfter.size()).isEqualTo(2);

        List<Batch> group1Batches = new ArrayList<>(batchGroupsAfter.get(0).getBatches().values());
        List<Batch> group2Batches = new ArrayList<>(batchGroupsAfter.get(1).getBatches().values());
        Assertions.assertThat(group1Batches.size()).isEqualTo(1);
        Assertions.assertThat(group2Batches.size()).isEqualTo(1);

        Assertions.assertThat(group1Batches.get(0).getEntities().size()).isEqualTo(1);
        Assertions.assertThat(group2Batches.get(0).getEntities().size()).isEqualTo(1);

        Assertions.assertThat(group1Batches.get(0).getEntities().contains(1)).isTrue();
        Assertions.assertThat(group2Batches.get(0).getEntities().contains(2)).isTrue();

        // change it back to obj1's material equivalent
        comp2.material.setNew(mat1);
        store.entityMaterialHasChanged(2, comp2);
        store.processChanges();

        List<BatchGroup> batchGroupsAfterRevert = new ArrayList<>(store.getGroups().values());
        Assertions.assertThat(batchGroupsAfterRevert.size()).isEqualTo(1);
        Assertions.assertThat(batchGroupsAfterRevert.get(0).getBatches().size()).isEqualTo(1);
        List<Batch> batchesAfterRevert = new ArrayList<>(batchGroupsAfterRevert.get(0).getBatches().values());
        Assertions.assertThat(batchesAfterRevert.get(0).getEntities().size()).isEqualTo(2);
    }

    @Test
    void changeMesh() {
        mockStandardRenderer();

        Material mat = new DefaultMaterial();
        Mesh mesh1 = createEmptyMesh();
        Mesh mesh2 = createEmptyMesh();

        RenderComponent comp2 = createMeshRendererComponent(mesh2, mat);

        store.add(1, createMeshRendererComponent(mesh1, mat));
        store.add(2, comp2);

        List<BatchGroup> batchGroups = new ArrayList<>(store.getGroups().values());
        Assertions.assertThat(batchGroups.size()).isEqualTo(1);

        Assertions.assertThat(batchGroups.get(0).getBatches().size()).isEqualTo(1);
        List<Batch> groupBatches = new ArrayList<>(batchGroups.get(0).getBatches().values());
        Assertions.assertThat(groupBatches.get(0).getEntities().size()).isEqualTo(2);

        // change obj2's mesh
        comp2.mesh.setIndices(new int[]{1, 2, 3});
        store.entityMeshHasChanged(2, comp2);
        store.processChanges();

        List<BatchGroup> batchGroupsAfter = new ArrayList<>(store.getGroups().values());
        Assertions.assertThat(batchGroupsAfter.size()).isEqualTo(1);

        Assertions.assertThat(batchGroupsAfter.get(0).getBatches().size()).isEqualTo(2);
        List<Batch> groupBatchesAfter = new ArrayList<>(batchGroupsAfter.get(0).getBatches().values());
        Assertions.assertThat(groupBatchesAfter.get(0).getEntities().size()).isEqualTo(1);
        Assertions.assertThat(groupBatchesAfter.get(1).getEntities().size()).isEqualTo(1);
        Assertions.assertThat(groupBatchesAfter.get(0).getEntities().contains(1)).isTrue();
        Assertions.assertThat(groupBatchesAfter.get(1).getEntities().contains(2)).isTrue();

        // change it back to obj1's mesh equivalent
        comp2.mesh.setNew(createEmptyMesh());
        store.entityMeshHasChanged(2, comp2);
        store.processChanges();

        List<BatchGroup> batchGroupsAfterRevert = new ArrayList<>(store.getGroups().values());
        Assertions.assertThat(batchGroupsAfterRevert.size()).isEqualTo(1);

        Assertions.assertThat(batchGroupsAfterRevert.get(0).getBatches().size()).isEqualTo(1);
        List<Batch> groupBatchesAfterRevert = new ArrayList<>(batchGroupsAfterRevert.get(0).getBatches().values());
        Assertions.assertThat(groupBatchesAfterRevert.get(0).getEntities().size()).isEqualTo(2);
    }

    @Test
    void changeMeshAndMaterial() {
        mockStandardRenderer();

        Material mat1 = new DefaultMaterial();
        Material mat2 = new DefaultMaterial();
        Mesh mesh1 = createEmptyMesh();
        Mesh mesh2 = createEmptyMesh();

        RenderComponent comp2 = createMeshRendererComponent(mesh2, mat2);

        store.add(1, createMeshRendererComponent(mesh1, mat1));
        store.add(2, comp2);

        List<BatchGroup> batchGroups = new ArrayList<>(store.getGroups().values());
        Assertions.assertThat(batchGroups.size()).isEqualTo(1);

        Assertions.assertThat(batchGroups.get(0).getBatches().size()).isEqualTo(1);
        List<Batch> groupBatches = new ArrayList<>(batchGroups.get(0).getBatches().values());
        Assertions.assertThat(groupBatches.get(0).getEntities().size()).isEqualTo(2);

        // change obj2's mesh and material
        comp2.mesh.setIndices(new int[]{1, 2, 3});
        comp2.material.setDiffuseColor(new Vector3f(0, 0, 0));
        store.entityMeshHasChanged(2, comp2);
        store.processChanges();

        List<BatchGroup> batchGroupsAfter = new ArrayList<>(store.getGroups().values());
        Assertions.assertThat(batchGroupsAfter.size()).isEqualTo(2);

        Assertions.assertThat(batchGroupsAfter.get(0).getBatches().size()).isEqualTo(1);
        Assertions.assertThat(batchGroupsAfter.get(1).getBatches().size()).isEqualTo(1);

        List<Batch> groupBatchesAfter1 = new ArrayList<>(batchGroupsAfter.get(0).getBatches().values());
        Assertions.assertThat(groupBatchesAfter1.get(0).getEntities().size()).isEqualTo(1);
        Assertions.assertThat(groupBatchesAfter1.get(0).getEntities().contains(1)).isTrue();

        List<Batch> groupBatchesAfter2 = new ArrayList<>(batchGroupsAfter.get(1).getBatches().values());
        Assertions.assertThat(groupBatchesAfter2.get(0).getEntities().size()).isEqualTo(1);
        Assertions.assertThat(groupBatchesAfter2.get(0).getEntities().contains(2)).isTrue();
    }

    private void mockStandardRenderer() {
        OpenGLStandardRenderer standardRenderer = Mockito.mock(OpenGLStandardRenderer.class);
        Engine.renderer = standardRenderer;
        Mockito.doNothing().when(standardRenderer).render(Mockito.any());
    }

    private Mesh createEmptyMesh() {
        return new Mesh(new float[]{}, new int[]{}, new float[]{}, new float[]{}, new ArrayList<>());
    }

    private RenderComponent createMeshRendererComponent(Mesh mesh, Material material) {
        RenderComponent component = new RenderComponent();
        component.mesh = mesh;
        component.material = material;
        component.transform = new Transform();
        return component;
    }
}

