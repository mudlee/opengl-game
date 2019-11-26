package spck.engine.ui.svg;

import org.lwjgl.nanovg.NSVGImage;
import org.lwjgl.system.MemoryStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spck.engine.util.ResourceLoader;

import java.nio.ByteBuffer;

import static org.lwjgl.nanovg.NanoSVG.nsvgParse;
import static org.lwjgl.system.MemoryUtil.memFree;

public class SVGLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(SVGLoader.class);

    public static NSVGImage load(String path) {
        LOGGER.debug("Loading svg from {}...", path);
        ByteBuffer source = ResourceLoader.loadToByteBuffer(path);
        NSVGImage svg;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            svg = nsvgParse(source, stack.ASCII("px"), 96.0f);
            if (svg == null) {
                throw new IllegalStateException("Failed to parse SVG.");
            }
            LOGGER.debug("SVG loaded. Width: {}, Height: {}", svg.width(), svg.height());
        } finally {
            memFree(source);
        }

        return svg;
    }
}
