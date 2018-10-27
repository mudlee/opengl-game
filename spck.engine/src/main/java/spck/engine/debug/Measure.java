package spck.engine.debug;

import spck.engine.bus.LifeCycle;
import spck.engine.bus.MessageBus;

public class Measure {
    private static final long UPDATE_RATE = 500_000_000L;
    private static long frameStart;
    private static long lastMeasure;
    private static int lastFPS;
    private static float lastRenderTime;
    private static float lastGraphicsRenderTime;
    private static long bufferSwapStart;

    public Measure() {
        MessageBus.register(LifeCycle.FRAME_START.eventID(), this::frameStart);
        MessageBus.register(LifeCycle.BEFORE_FRAME_SYNC.eventID(), this::beforeFrameSync);
        MessageBus.register(LifeCycle.AFTER_FRAME_SYNC.eventID(), this::afterFrameSync);
        MessageBus.register(LifeCycle.BEFORE_BUFFER_SWAP.eventID(), this::beforeBufferSwap);
        MessageBus.register(LifeCycle.AFTER_BUFFER_SWAP.eventID(), this::afterBufferSwap);
    }

    public static int getLastFPS() {
        return lastFPS;
    }

    public static float getLastRenderTime() {
        return lastRenderTime;
    }

    public static float getLastGraphicsRenderTime() {
        return lastGraphicsRenderTime;
    }

    private void frameStart() {
        frameStart = System.nanoTime();
    }

    private void beforeFrameSync() {
        if (shouldNotMeasure()) {
            return;
        }

        lastRenderTime = (System.nanoTime() - frameStart) / 1_000_000f;
    }

    private void afterFrameSync() {
        if (shouldNotMeasure()) {
            return;
        }

        lastMeasure = System.nanoTime();


        long frameTime = System.nanoTime() - frameStart;
        lastFPS = ((Math.round(1_000_000_000L / frameTime) + 5) / 10) * 10;
    }

    private void beforeBufferSwap() {
        bufferSwapStart = System.nanoTime();
    }

    private void afterBufferSwap() {
        if (shouldNotMeasure()) {
            return;
        }

        lastGraphicsRenderTime = (System.nanoTime() - bufferSwapStart) / 1_000_000f;
    }

    private static boolean shouldNotMeasure() {
        return (lastMeasure + UPDATE_RATE) > System.nanoTime();
    }
}
