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
import spck.engine.render.DefaultMaterial;
import spck.engine.render.Material;
import spck.engine.render.Mesh;
import spck.engine.render.ShaderUniform;
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

    public static ModelInfo load(String resourcePath) {
        RunOnce.run("ModelLoader CleanUp", () -> {
            MessageBus.register(LifeCycle.CLEANUP.eventID(), ModelLoader::cleanUp);
        });

        LOGGER.debug("# Trying to load model {}...", resourcePath);

        AIScene scene;
        String extension = resourcePath.substring(resourcePath.lastIndexOf("."));

        if (modelCache.containsKey(resourcePath)) {
            LOGGER.debug("    Loading from cache");
            scene = modelCache.get(resourcePath);
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

        List<ModelPart> parts = loadModelParts(scene, resourcePath, extension);
        LOGGER.debug("    Model {} has been loaded. Contains {} meshes", resourcePath, parts.size());
        return new ModelInfo(parts);
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

    private static List<ModelPart> loadModelParts(AIScene scene, String resourcePath, String extension) {
        List<ModelPart> parts = new ArrayList<>();

        int numMeshes = scene.mNumMeshes();
        int numMaterials = scene.mNumMaterials();
        LOGGER.debug("    Found {} meshes...", numMeshes);
        LOGGER.debug("    Found {} materials...", numMaterials);

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
            LOGGER.debug("    Processing material at index {}...", i);
            AIMaterial aiMaterial = AIMaterial.create(aiMaterials.get(i));
            materials.add(processMaterial(aiMaterial, resourcePath, extension));
            LOGGER.debug("    Material loaded");
        }

        for (int i = 0; i < numMeshes; i++) {
            LOGGER.debug("    Processing mesh at index {}...", i);
            AIMesh aiMesh = AIMesh.create(aiMeshes.get(i));

            int materialIndex = aiMesh.mMaterialIndex();

            Mesh mesh = new Mesh(
                    getVerticesFromMesh(aiMesh),
                    getIndicesFromMesh(aiMesh),
                    getNormalsFromMesh(aiMesh),
                    getUVCoordsFromMesh(aiMesh),
                    new ArrayList<>()
            );

            LOGGER.debug("    Mesh has been loaded. Verts: {}, normals: {}, mat idx: {}", mesh.getIndices().length, mesh.getNormals().length / 3, materialIndex);
            Material material = materials.isEmpty() ? new DefaultMaterial() : materials.get(materialIndex);
            parts.add(new ModelPart(mesh, material));
        }

        return parts;
    }

    private static Material processMaterial(AIMaterial aiMaterial, String modelPath, String extension) {
        DefaultMaterial material = new DefaultMaterial();
        material.setDiffuseColor(getMaterialColor(aiMaterial, Assimp.AI_MATKEY_COLOR_DIFFUSE));
        material.setSpecularColor(getMaterialColor(aiMaterial, Assimp.AI_MATKEY_COLOR_SPECULAR));
        material.setAmbientColor(getMaterialColor(aiMaterial, Assimp.AI_MATKEY_COLOR_AMBIENT));
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
                    textPath,
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

    private static Vector3f getMaterialColor(AIMaterial aiMaterial, String colorType) {
        AIColor4D color = AIColor4D.create();
        Assimp.aiGetMaterialColor(aiMaterial, colorType, Assimp.aiTextureType_NONE, 0, color);
        Vector3f matColor = new Vector3f(color.r(), color.g(), color.b());
        LOGGER.debug("    Material {} color is {}", colorType, matColor);
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