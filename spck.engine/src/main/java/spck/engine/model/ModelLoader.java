package spck.engine.model;

import org.joml.Vector3f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.AIColor4D;
import org.lwjgl.assimp.AIFace;
import org.lwjgl.assimp.AIMaterial;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AIScene;
import org.lwjgl.assimp.AIString;
import org.lwjgl.assimp.AIVector3D;
import org.lwjgl.assimp.Assimp;
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

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
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

        LOGGER.trace("# Trying to load model {}...", resourcePath);

        AIScene scene;
        String extension = resourcePath.substring(resourcePath.lastIndexOf("."));

        if (modelCache.containsKey(resourcePath)) {
            LOGGER.trace("    Loading from cache");
            scene = modelCache.get(resourcePath);
        } else {
            LOGGER.trace("    Extension: {}", extension);

            URL res = ModelLoader.class.getResource(resourcePath);
            String modelPath = getmodelPath(res.getPath());

            LOGGER.trace("    Loading model from PATH->'{}', RES->'{}'", modelPath, res.toString());

            if (res.toString().startsWith("jar:") || res.toString().startsWith("jrt:")) {
                modelPath = unpackModelsFromJar(resourcePath, extension);
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

        // MESH
        Mesh mesh = loadMesh(scene, resourcePath);

        // MATERIAL
        Material material = loadMaterial(scene, mesh.getMaterialIndex(), resourcePath, extension);

        LOGGER.trace("    Model {} has been loaded. Mesh: {}, Material: {}", resourcePath, mesh, material);
        return new ModelInfo(mesh, material);
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

    private static Material loadMaterial(AIScene scene, int materialIndex, String resourcePath, String extension) {
        int numMaterials = scene.mNumMaterials();
        Material material;

        if (numMaterials == 0) {
            LOGGER.trace("    Mesh has no materials defined, using the default one");
            material = new DefaultMaterial();
        } else {
            LOGGER.trace("    Processing material...");
            PointerBuffer aiMaterials = scene.mMaterials();

            if (aiMaterials == null) {
                throw new RuntimeException("aiMaterials PointBuffer was null for model " + resourcePath);
            }

            AIMaterial aiMaterial = AIMaterial.create(aiMaterials.get(materialIndex));
            material = processMaterial(aiMaterial, resourcePath, extension);
            LOGGER.trace("    Material loaded");
        }
        return material;
    }

    private static Mesh loadMesh(AIScene scene, String resourcePath) {
        int numMeshes = scene.mNumMeshes();
        LOGGER.trace("    Found {} meshes...", numMeshes);
        PointerBuffer aiMeshes = scene.mMeshes();

        if (aiMeshes == null) {
            throw new RuntimeException("aiMeshes PointBuffer was null for model " + resourcePath);
        }

        if (numMeshes == 0) {
            throw new RuntimeException("Could not find any mesh for model " + resourcePath);
        }

        // we care only about the first mesh
        LOGGER.trace("    Processing mesh at index 0...");
        AIMesh aiMesh = AIMesh.create(aiMeshes.get(0));

        int materialIndex = aiMesh.mMaterialIndex();

        Mesh mesh = new Mesh(
                processVertices(aiMesh),
                processIndices(aiMesh),
                processNormals(aiMesh),
                processUVCoords(aiMesh),
                new ArrayList<>(),
                materialIndex
        );

        LOGGER.trace("    Mesh has been loaded. Verts: {}, normals: {}, mat idx: {}", mesh.getIndices().length, mesh.getNormals().length / 3, materialIndex);
        return mesh;
    }

    private static String unpackModelsFromJar(String resourcePath, String extension) {
        String origName = resourcePath.substring(resourcePath.lastIndexOf("/") + 1).replace(extension, "");

        try {
            final String tempDir = System.getProperty("java.io.tmpdir");
            LOGGER.trace("    Model found in a jar file, extracting files to a temp directory: {}", tempDir);

            File modelFile = new File(tempDir, origName + extension);
            modelFile.deleteOnExit();
            modelFile.createNewFile();

            InputStream modelInput = ModelLoader.class.getResourceAsStream(resourcePath);
            Files.write(modelFile.toPath(), modelInput.readAllBytes());
            LOGGER.trace("    File {} extracted", tempDir + origName + extension);

            // MTL
            File mtlFile = new File(tempDir, origName + ".mtl");
            mtlFile.deleteOnExit();
            mtlFile.createNewFile();

            InputStream mtlInput = ModelLoader.class.getResourceAsStream(resourcePath.replace(extension, ".mtl"));
            if (mtlInput == null) {
                LOGGER.trace("    MTL file was not found for {}, skipping", origName);
            } else {
                Files.write(mtlFile.toPath(), mtlInput.readAllBytes());
                LOGGER.trace("    File {} extracted", origName + ".mtl");
            }

            // TEXTURE
            File textureFile = new File(tempDir, origName + ".png");
            textureFile.deleteOnExit();
            textureFile.createNewFile();

            InputStream textureInput = ModelLoader.class.getResourceAsStream(resourcePath.replace(extension, ".png"));
            if (textureInput == null) {
                LOGGER.trace("    Texture file was not found for {}, skipping", origName);
            } else {
                Files.write(textureFile.toPath(), textureInput.readAllBytes());
                LOGGER.trace("    File {} extracted", origName + ".png");
            }

            return modelFile.getPath();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

        LOGGER.trace("    Trying to load texture from {}", textPath);
        try {
            ByteBuffer buffer = ResourceLoader.loadToByteBuffer(textPath);
            Texture2D texture = (Texture2D) TextureRegistry.register(TextureStorage.loadFromTextureData(
                    TextureLoader.loadFromByteBuffer(buffer),
                    textPath,
                    ShaderUniform.Material.DIFFUSE_TEXTURE_SAMPLER.getUniformName(),
                    new ModelTextureRegistryID(textPath)
            ));
            LOGGER.trace("    Material {} texture is {}, - {}", textureType, textPath, texture);
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
        LOGGER.trace("    Material {} color is {}", colorType, matColor);
        return matColor;
    }

    private static float[] processVertices(AIMesh aiMesh) {
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

    private static float[] processNormals(AIMesh aiMesh) {
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

    private static int[] processIndices(AIMesh aiMesh) {
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

    private static float[] processUVCoords(AIMesh aiMesh) {
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