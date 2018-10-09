package spck.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spck.engine.core.GameLoop;
import spck.engine.core.OS;
import spck.engine.core.Window;
import spck.engine.util.OSNameParser;
import spck.engine.vulkan.VulkanUtil;

public class Engine implements Runnable{
    private static final Logger LOGGER = LoggerFactory.getLogger(Engine.class);
    private final Thread GAME_LOOP_THREAD;
    private final Window window=new Window();
    private final VulkanUtil vulkanUtil = new VulkanUtil();
    private final GameLoop gameLoop=new GameLoop();
    private final OS os;

    public Engine() {
        LOGGER.debug("Creating GAME_LOOP_THREAD...");
        this.GAME_LOOP_THREAD=new Thread(this,"GAME_LOOP_THREAD");

        os = OSNameParser.parse(System.getProperty("os.name"));
    }

    public void launch() {
        LOGGER.debug("Launching game...");
        if ( System.getProperty("os.name").contains("Mac") ) {
            GAME_LOOP_THREAD.run();
        }
        else {
            GAME_LOOP_THREAD.start();
        }
    }

    @Override
    public void run() {
        window.init();
        vulkanUtil.init(os);

        gameLoop.init(window.getWindowID());
        gameLoop.loop();

        vulkanUtil.cleanup();
        window.cleanup();
    }
}
