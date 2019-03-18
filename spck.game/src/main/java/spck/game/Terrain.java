package spck.game;

import org.joml.Vector3f;
import spck.engine.ecs.Entity;
import spck.engine.ecs.physics.Physics3DBodyComponent;
import spck.engine.ecs.render.components.RenderComponent;
import spck.engine.render.DefaultMaterial;
import spck.engine.render.Mesh;
import spck.engine.render.MeshTriangle;
import spck.engine.render.Transform;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Terrain extends Entity {
    @Override
    public void onInit() {
        RenderComponent cRender = addComponent(RenderComponent.class);
        cRender.material = new DefaultMaterial();
        cRender.mesh = createMesh();
        cRender.transform = new Transform();

        Physics3DBodyComponent cPhysics3D = addComponent(Physics3DBodyComponent.class);
    }

    private Mesh createMesh() {
        int size = 300;
        List<Vector3f> vertices = new ArrayList<>();
        List<MeshTriangle> tris = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        float[][] noiseMap = noiseMap(size + 1, size + 1);

        int indicesIndex = 0;
        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                float vertex1Height = noiseMap[x][z + 1];
                float vertex2Height = noiseMap[x + 1][z + 1];
                float vertex3Height = noiseMap[x][z];

                float vertex4Height = noiseMap[x + 1][z + 1];
                float vertex5Height = noiseMap[x + 1][z];
                float vertex6Height = noiseMap[x][z];

                Vector3f vert1 = new Vector3f(x, vertex1Height, z + 1);
                Vector3f vert2 = new Vector3f(x + 1, vertex2Height, z + 1);
                Vector3f vert3 = new Vector3f(x, vertex3Height, z);
                vertices.add(vert1); // top left
                vertices.add(vert2); // top right
                vertices.add(vert3); // bottom left
                tris.add(new MeshTriangle(vert1, vert2, vert3));

                Vector3f vert4 = new Vector3f(x + 1, vertex4Height, z + 1);
                Vector3f vert5 = new Vector3f(x + 1, vertex5Height, z);
                Vector3f vert6 = new Vector3f(x, vertex6Height, z);
                vertices.add(vert4); // top right
                vertices.add(vert5); // bottom right
                vertices.add(vert6); // bottom left
                tris.add(new MeshTriangle(vert4, vert5, vert6));

                indices.add(indicesIndex++);
                indices.add(indicesIndex++);
                indices.add(indicesIndex++);
                indices.add(indicesIndex++);
                indices.add(indicesIndex++);
                indices.add(indicesIndex++);
            }
        }

        float[] objVertices = new float[vertices.size() * 3];
        int verticesIndex = 0;
        for (Vector3f vertex : vertices) {
            objVertices[verticesIndex++] = vertex.x;
            objVertices[verticesIndex++] = vertex.y;
            objVertices[verticesIndex++] = vertex.z;
        }

        int[] objIndices = new int[indices.size()];
        indicesIndex = 0;
        for (Integer index : indices) {
            objIndices[indicesIndex++] = index;
        }

        Mesh mesh = new Mesh(
                objVertices,
                objIndices,
                new float[objVertices.length],
                new float[]{},
                tris
        );

        mesh.recalculateNormals();
        return mesh;
    }

    private static float[][] noiseMap(int width, int height) {
        float[][] result = new float[width][height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                result[x][y] = Double.valueOf(ThreadLocalRandom.current().nextDouble(-0.1d, 0.1d)).floatValue();
            }
        }

        return result;
    }
}
