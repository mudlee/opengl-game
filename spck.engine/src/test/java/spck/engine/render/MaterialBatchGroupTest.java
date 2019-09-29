package spck.engine.render;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Map;

class MaterialBatchGroupTest {
    private MaterialBatchGroup group;

    @BeforeEach
    void before() {
        group = new MaterialBatchGroup(Mockito.mock(DefaultMaterial.class));
    }

    @Test
    void containsWorks() {
        group.addBatch(1, Mockito.mock(MeshMaterialBatch.class));
        Assertions.assertThat(group.containsBatch(1)).isEqualTo(true);
        Assertions.assertThat(group.containsBatch(2)).isEqualTo(false);
    }

    @Test
    void batchCannotBeAddedTwice() {
        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> {
            group.addBatch(1, Mockito.mock(MeshMaterialBatch.class));
            group.addBatch(1, Mockito.mock(MeshMaterialBatch.class));
        });
    }

    @Test
    void getBatchWorks() {
        group.addBatch(1, Mockito.mock(MeshMaterialBatch.class));
        Assertions.assertThat(group.getBatch(1).isPresent()).isEqualTo(true);
        Assertions.assertThat(group.getBatch(2).isPresent()).isEqualTo(false);
    }

    @Test
    void getBatchesWorks() {
        group.addBatch(1, Mockito.mock(MeshMaterialBatch.class));
        group.addBatch(2, Mockito.mock(MeshMaterialBatch.class));

        Map<Integer, MeshMaterialBatch> batches = group.getBatches();
        Assertions.assertThat(batches.size()).isEqualTo(2);
    }
}
