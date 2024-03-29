package spck.engine.render;

import org.joml.AABBf;
import org.joml.Vector3f;

import java.util.Arrays;

public class Mesh {
    private static final Vector3f REUSABLE_3D_VECTOR_A = new Vector3f().zero();
    private static final Vector3f REUSABLE_3D_VECTOR_B = new Vector3f().zero();
    private static final Vector3f REUSABLE_3D_VECTOR_C = new Vector3f().zero();

    private float[] vertices;
    private int[] indices;
    private float[] normals;
    private float[] uvCoords;
    private final AABBf aabb;
    private boolean changed;

    public Mesh(float[] vertices, int[] indices, float[] normals, float[] uvCoords, AABBf aabb) {
        this.vertices = vertices;
        this.indices = indices;
        this.normals = normals;
        this.uvCoords = uvCoords;
        this.aabb = aabb;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Mesh)) return false;
        Mesh mesh = (Mesh) o;
        return Arrays.equals(vertices, mesh.vertices) &&
                Arrays.equals(indices, mesh.indices) &&
                Arrays.equals(normals, mesh.normals);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(vertices);
        result = 31 * result + Arrays.hashCode(indices);
        result = 31 * result + Arrays.hashCode(normals);
        return result;
    }

    public void setNew(Mesh mesh) {
        vertices = mesh.vertices;
        indices = mesh.indices;
        normals = mesh.normals;
        uvCoords = mesh.uvCoords;
        changed = true;
    }

    public boolean isChanged() {
        return changed;
    }

    public void ackChanges() {
        if (changed) {
            changed = false;
        }
    }

    public void setUvCoords(float[] uvCoords) {
        this.uvCoords = uvCoords;
        changed = true;
    }

    public void setVertices(float[] vertices) {
        this.vertices = vertices;
        changed = true;
    }

    public void setIndices(int[] indices) {
        this.indices = indices;
        changed = true;
    }

    public void setNormals(float[] normals) {
        this.normals = normals;
        changed = true;
    }

    public float[] getUVCoords() {
        return uvCoords;
    }

    public float[] getVertices() {
        return vertices;
    }

    public int[] getIndices() {
        return indices;
    }

    public float[] getNormals() {
        return normals;
    }

    public AABBf getAABB() {
        return aabb;
    }

    public void recalculateNormals() {
        for (int i = 0; i < vertices.length; i += 3 * 3) { // <- 3*3 = one triangle/face per iteration
            REUSABLE_3D_VECTOR_A.set(vertices[i], vertices[i + 1], vertices[i + 2]);
            REUSABLE_3D_VECTOR_B.set(vertices[i + 3], vertices[i + 4], vertices[i + 5]);
            REUSABLE_3D_VECTOR_C.set(vertices[i + 6], vertices[i + 7], vertices[i + 8]);
            Vector3f n = REUSABLE_3D_VECTOR_B.sub(REUSABLE_3D_VECTOR_A).cross(REUSABLE_3D_VECTOR_C.sub(REUSABLE_3D_VECTOR_A)).normalize();
            // Assign that normal to all vertices of the current triangle/face
            for (int j = 0; j < 3; j++) {
                normals[i + 3 * j] = n.x;
                normals[i + 3 * j + 1] = n.y;
                normals[i + 3 * j + 2] = n.z;
            }
        }

        changed = true;
    }
}

