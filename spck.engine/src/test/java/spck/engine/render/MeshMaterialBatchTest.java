package spck.engine.render;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class MeshMaterialBatchTest {
    private MeshMaterialBatch batch;

    @BeforeEach
    void before() {
        batch = new MeshMaterialBatch(Mockito.mock(Mesh.class), Mockito.mock(Material.class));
    }

    @Test
    void addWorks() {
        batch.addEntity(1);
        Assertions.assertThat(batch.getNumOfEntities()).isEqualTo(1);
        Assertions.assertThat(batch.getEntities().size()).isEqualTo(1);
        Assertions.assertThat(batch.getOldSize()).isEqualTo(0);
        Assertions.assertThat(batch.wasSizeChanged()).isEqualTo(true);
    }

    @Test
    void cannotAddTwice() {
        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> {
            batch.addEntity(1);
            batch.addEntity(1);
        });
    }

    @Test
    void removeWorks() {
        batch.addEntity(1);
        batch.addEntity(2);

        Assertions.assertThat(batch.getNumOfEntities()).isEqualTo(2);
        Assertions.assertThat(batch.getEntities().size()).isEqualTo(2);
        Assertions.assertThat(batch.getOldSize()).isEqualTo(0);
        Assertions.assertThat(batch.wasSizeChanged()).isEqualTo(true);

        batch.removeEntity(1);

        Assertions.assertThat(batch.getNumOfEntities()).isEqualTo(1);
        Assertions.assertThat(batch.getEntities().size()).isEqualTo(1);
        Assertions.assertThat(batch.getEntities().contains(2)).isTrue();
        Assertions.assertThat(batch.getOldSize()).isEqualTo(0);
        Assertions.assertThat(batch.wasSizeChanged()).isEqualTo(true);
    }

    @Test
    void dataUpdatedWorks() {
        batch.addEntity(1);
        batch.addEntity(2);

        Assertions.assertThat(batch.getNumOfEntities()).isEqualTo(2);
        Assertions.assertThat(batch.getEntities().size()).isEqualTo(2);
        Assertions.assertThat(batch.getOldSize()).isEqualTo(0);
        Assertions.assertThat(batch.wasSizeChanged()).isEqualTo(true);

        batch.dataUpdated();

        Assertions.assertThat(batch.getOldSize()).isEqualTo(2);
        Assertions.assertThat(batch.wasSizeChanged()).isEqualTo(false);
    }
}
