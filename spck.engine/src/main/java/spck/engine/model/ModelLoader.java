package spck.engine.model;

import org.joml.Vector3f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spck.engine.Engine;
import spck.engine.OS;
import spck.engine.bus.LifeCycle;
import spck.engine.bus.MessageBus;
import spck.engine.framework.assets.TextureLoader;
import spck.engine.framework.assets.TextureStorage;
import spck.engine.render.*;
import spck.engine.render.shader.ShaderUniform;
import spck.engine.render.textures.Texture2D;
import spck.engine.render.textures.TextureRegistry;
import spck.engine.util.ResourceLoader;
import spck.engine.util.RunOnce;

import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModelLoader {
    private final static Logger LOGGER = LoggerFactory.getLogger(ModelLoader.class);
    private final static int IMPORT_FLAGS = Assimp.aiProcess_JoinIdenticalVertices | Assimp.aiProcess_Triangulate | Assimp.aiProcess_FixInfacingNormals | Assimp.aiProcess_OptimizeMeshes;

    private final static Map<String, AIScene> modelCache = new HashMap<>();

    public static MeshMaterialCollection load(String resourcePath) {
        RunOnce.run("ModelLoader CleanUp", () -> {
            MessageBus.register(LifeCycle.CLEANUP.eventID(), ModelLoader::cleanUp);
        });

        LOGGER.debug("# Trying to load model {}...", resourcePath);

        AIScene scene;
        boolean fromCache = false;
        String extension = resourcePath.substring(resourcePath.lastIndexOf("."));

        if (modelCache.containsKey(resourcePath)) {
            LOGGER.debug("    Loading from cache");
            scene = modelCache.get(resourcePath);
            fromCache = true;
        } else {
            LOGGER.debug("    Extension: {}", extension);

            URL res = ModelLoader.class.getResource(resourcePath);
            if (res == null) {
                throw new RuntimeException(String.format("Model could not be find: %s", resourcePath));
            }
            String modelPath = getmodelPath(res.getPath());

            LOGGER.debug("    Loading model from PATH->'{}', RES->'{}'", modelPath, res.toString());

            if (res.toString().startsWith("jar:") || res.toString().startsWith("jrt:")) {
                modelPath = ModelJarUtil.unpackFromJar(resourcePath, extension);
            }

            scene = Assimp.aiImportFile(
                    modelPath,
                    IMPORT_FLAGS
            );

            if (scene == null) {
                String error = Assimp.aiGetErrorString();
                throw new RuntimeException(String.format("Could not load model from %s. Error: %s", modelPath, error));
            }

            modelCache.put(resourcePath, scene);
        }

        List<ModelPart> parts = loadModelParts(scene, resourcePath, extension, fromCache);
        LOGGER.debug("    Model {} has been loaded. Contains {} meshes", resourcePath, parts.size());
        List<MeshMaterialPair> meshMaterialPairs = new ArrayList<>();
        for (ModelPart part : parts) {
            meshMaterialPairs.add(new MeshMaterialPair(part.getMesh(), part.getMaterial()));
        }
        return new MeshMaterialCollection(meshMaterialPairs);
    }

    private static void cleanUp() {
        modelCache.values().forEach(Assimp::aiReleaseImport);
    }

    private static String getmodelPath(String path) {
        if (Engine.preferences.os == OS.WINDOWS) {
            return path.substring(1);
        }

        return path;
    }

    private static List<ModelPart> loadModelParts(AIScene scene, String resourcePath, String extension, boolean fromCache) {
        List<ModelPart> parts = new ArrayList<>();

        int numMeshes = scene.mNumMeshes();
        int numMaterials = scene.mNumMaterials();
        if (!fromCache) {
            LOGGER.debug("    Found {} meshes...", numMeshes);
            LOGGER.debug("    Found {} materials...", numMaterials);
        }

        PointerBuffer aiMeshes = scene.mMeshes();

        if (aiMeshes == null) {
            throw new RuntimeException("aiMeshes PointBuffer was null for model " + resourcePath);
        }

        if (numMeshes == 0) {
            throw new RuntimeException("Could not find any mesh for model " + resourcePath);
        }

        List<Material> materials = new ArrayList<>();
        PointerBuffer aiMaterials = scene.mMaterials();

        if (aiMaterials == null) {
            throw new RuntimeException("aiMaterials PointBuffer was null for model " + resourcePath);
        }

        for (int i = 0; i < numMaterials; i++) {
            if (!fromCache) LOGGER.debug("    Processing material at index {}...", i);
            AIMaterial aiMaterial = AIMaterial.create(aiMaterials.get(i));
            materials.add(processMaterial(aiMaterial, resourcePath, extension, fromCache));
            if (!fromCache) LOGGER.debug("    Material loaded");
        }

        for (int i = 0; i < numMeshes; i++) {
            if (!fromCache) LOGGER.debug("    Processing mesh at index {}...", i);
            AIMesh aiMesh = AIMesh.create(aiMeshes.get(i));

            int materialIndex = aiMesh.mMaterialIndex();

            Mesh mesh = new Mesh(
                    getVerticesFromMesh(aiMesh),
                    getIndicesFromMesh(aiMesh),
                    getNormalsFromMesh(aiMesh),
                    getUVCoordsFromMesh(aiMesh),
                    calculateAABB(aiMesh)
            );

            if (!fromCache)
                LOGGER.debug("    Mesh has been loaded. Verts: {}, normals: {}, mat idx: {}", mesh.getIndices().length, mesh.getNormals().length / 3, materialIndex);
            Material material = materials.isEmpty() ? new DefaultMaterial() : materials.get(materialIndex);
            parts.add(new ModelPart(mesh, material));
        }

        return parts;
    }

    private static AABB calculateAABB(AIMesh aiMesh) {
        boolean initiated = false;
        Vector3f min = new Vector3f();
        Vector3f max = new Vector3f();

        AIVector3D.Buffer aiVertices = aiMesh.mVertices();
        while (aiVertices.remaining() > 0) {
            AIVector3D aiVertex = aiVertices.get();

            if (!initiated) {
                min.set(aiVertex.x(), aiVertex.y(), aiVertex.z());
                max.set(aiVertex.x(), aiVertex.y(), aiVertex.z());
                initiated = true;
                continue;
            }

            if (aiVertex.x() < min.x) {
                min.x = aiVertex.x();
            } else if (aiVertex.x() > max.x) {
                max.x = aiVertex.x();
            }

            if (aiVertex.y() < min.y) {
                min.y = aiVertex.y();
            } else if (aiVertex.y() > max.y) {
                max.y = aiVertex.y();
            }

            if (aiVertex.z() < min.z) {
                min.z = aiVertex.z();
            } else if (aiVertex.z() > max.z) {
                max.z = aiVertex.z();
            }
        }

        return new AABB(min, max);
    }

    private static Material processMaterial(AIMaterial aiMaterial, String modelPath, String extension, boolean fromCache) {
        DefaultMaterial material = new DefaultMaterial();
        material.setDiffuseColor(getMaterialColor(aiMaterial, Assimp.AI_MATKEY_COLOR_DIFFUSE, fromCache));
        material.setSpecularColor(getMaterialColor(aiMaterial, Assimp.AI_MATKEY_COLOR_SPECULAR, fromCache));
        material.setAmbientColor(getMaterialColor(aiMaterial, Assimp.AI_MATKEY_COLOR_AMBIENT, fromCache));
        material.setDiffuseTexture(getMaterialTexture(aiMaterial, Assimp.aiTextureType_DIFFUSE, modelPath, extension));

        return material;
    }

    // NOTE: it only loads textures with the same name, currently
    private static Texture2D getMaterialTexture(AIMaterial aiMaterial, int textureType, String modelPath, String extension) {
        AIString path = AIString.calloc();
        int result = Assimp.aiGetMaterialTexture(aiMaterial, textureType, 0, path, (IntBuffer) null, null, null, null, null, null);
        if (result != Assimp.aiReturn_SUCCESS) {
            return null;
        }

        String textPath = modelPath.replace(extension, ".png");

        LOGGER.debug("    Trying to load texture from {}", textPath);
        try {
            ByteBuffer buffer = ResourceLoader.loadToByteBuffer(textPath);
            Texture2D texture = (Texture2D) TextureRegistry.register(TextureStorage.loadFromTextureData(
                    TextureLoader.loadFromByteBuffer(buffer),
                    ShaderUniform.Material.DIFFUSE_TEXTURE_SAMPLER.getUniformName(),
                    new ModelTextureRegistryID(textPath)
            ));
            LOGGER.debug("    Material {} texture is {}, - {}", textureType, textPath, texture);
            return texture;
        } catch (Exception e) {
            LOGGER.error("    Texture was not found at {}", textPath);
            throw new RuntimeException(e);
        }
    }

    private static Vector3f getMaterialColor(AIMaterial aiMaterial, String colorType, boolean fromCache) {
        AIColor4D color = AIColor4D.create();
        Assimp.aiGetMaterialColor(aiMaterial, colorType, Assimp.aiTextureType_NONE, 0, color);
        Vector3f matColor = new Vector3f(color.r(), color.g(), color.b());
        if (!fromCache) LOGGER.debug("    Material {} color is {}", colorType, matColor);
        return matColor;
    }

    private static float[] getVerticesFromMesh(AIMesh aiMesh) {
        AIVector3D.Buffer aiVertices = aiMesh.mVertices();
        float[] result = new float[aiVertices.capacity() * 3];

        int i = 0;
        while (aiVertices.remaining() > 0) {
            AIVector3D aiVertex = aiVertices.get();
            result[i++] = aiVertex.x();
            result[i++] = aiVertex.y();
            result[i++] = aiVertex.z();
        }

        return result;
    }

    private static float[] getNormalsFromMesh(AIMesh aiMesh) {
        AIVector3D.Buffer aiNormals = aiMesh.mNormals();

        if (aiNormals == null) {
            return null;
        }

        float[] result = new float[aiNormals.capacity() * 3];

        int i = 0;
        while (aiNormals.remaining() > 0) {
            AIVector3D aiNormal = aiNormals.get();
            result[i++] = aiNormal.x();
            result[i++] = aiNormal.y();
            result[i++] = aiNormal.z();
        }

        return result;
    }

    private static int[] getIndicesFromMesh(AIMesh aiMesh) {
        int numFaces = aiMesh.mNumFaces();
        AIFace.Buffer aiFaces = aiMesh.mFaces();

        List<Integer> indices = new ArrayList<>();

        for (int i = 0; i < numFaces; i++) {
            AIFace aiFace = aiFaces.get(i);
            IntBuffer buffer = aiFace.mIndices();
            while (buffer.remaining() > 0) {
                indices.add(buffer.get());
            }
        }

        int[] result = new int[indices.size()];
        for (int i = 0; i < indices.size(); i++) {
            result[i] = indices.get(i);
        }

        return result;
    }

    private static float[] getUVCoordsFromMesh(AIMesh aiMesh) {
        AIVector3D.Buffer uvCoords = aiMesh.mTextureCoords(0);
        int numUVCoords = uvCoords != null ? uvCoords.remaining() : 0;

        if (numUVCoords == 0) {
            return new float[]{};
        }

        float[] coords = new float[numUVCoords * 2];

        for (int i = 0; i < numUVCoords * 2; i += 2) {
            AIVector3D textCoord = uvCoords.get();
            coords[i] = textCoord.x();
            coords[i + 1] = 1 - textCoord.y();
        }

        return coords;
    }
}