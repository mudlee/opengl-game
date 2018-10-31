package spck.engine.render;

import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class MeshTriangle {
    private List<Vector3f> vertices = new ArrayList<>();
    private List<Integer> vertexIndices = new ArrayList<>();
    private List<Vector3f> vertexNormals = new ArrayList<>();

    public MeshTriangle(Vector3f vert1, Vector3f vert2, Vector3f vert3) {
        addVertex(vert1);
        addVertex(vert2);
        addVertex(vert3);
    }

    @Override
    public String toString() {
        return "MeshTriangle{" +
                "vertices=" + vertices +
                ", vertexIndices=" + vertexIndices +
                ", vertexNormals=" + vertexNormals +
                '}';
    }

    public void addVertex(Vector3f vertex) {
        checkSize(vertices);
        vertices.add(vertex);
    }

    public void addVertexIndex(int index) {
        checkSize(vertexIndices);
        vertexIndices.add(index);
    }

    public void addVertexNormal(Vector3f normal) {
        checkSize(vertexNormals);
        vertexNormals.add(normal);
    }

    public List<Vector3f> getVertices() {
        return vertices;
    }

    public List<Integer> getVertexIndices() {
        return vertexIndices;
    }

    public List<Vector3f> getVertexNormals() {
        return vertexNormals;
    }

    private void checkSize(List<?> list) {
        if (list.size() == 3) {
            throw new RuntimeException("Triangle has only 3 vertices");
        }
    }
}
