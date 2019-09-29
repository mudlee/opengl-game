package spck.engine.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;

public class ModelJarUtil {
    private final static Logger LOGGER = LoggerFactory.getLogger(ModelJarUtil.class);

    public static String unpackFromJar(String resourcePath, String extension) {
        String origName = resourcePath.substring(resourcePath.lastIndexOf("/") + 1).replace(extension, "");

        try {
            final String tempDir = System.getProperty("java.io.tmpdir");
            LOGGER.debug("    Model found in a jar file, extracting files to a temp directory: {}", tempDir);

            File modelFile = new File(tempDir, origName + extension);
            modelFile.deleteOnExit();
            modelFile.createNewFile();

            InputStream modelInput = ModelLoader.class.getResourceAsStream(resourcePath);
            Files.write(modelFile.toPath(), modelInput.readAllBytes());
            LOGGER.debug("    File {} extracted", tempDir + origName + extension);

            // MTL
            File mtlFile = new File(tempDir, origName + ".mtl");
            mtlFile.deleteOnExit();
            mtlFile.createNewFile();

            InputStream mtlInput = ModelLoader.class.getResourceAsStream(resourcePath.replace(extension, ".mtl"));
            if (mtlInput == null) {
                LOGGER.debug("    MTL file was not found for {}, skipping", origName);
            } else {
                Files.write(mtlFile.toPath(), mtlInput.readAllBytes());
                LOGGER.debug("    File {} extracted", origName + ".mtl");
            }

            // TEXTURE
            File textureFile = new File(tempDir, origName + ".png");
            textureFile.deleteOnExit();
            textureFile.createNewFile();

            InputStream textureInput = ModelLoader.class.getResourceAsStream(resourcePath.replace(extension, ".png"));
            if (textureInput == null) {
                LOGGER.debug("    Texture file was not found for {}, skipping", origName);
            } else {
                Files.write(textureFile.toPath(), textureInput.readAllBytes());
                LOGGER.debug("    File {} extracted", origName + ".png");
            }

            return modelFile.getPath();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
