package spck.engine;

public class Time {
    public static float deltaTime;

    public static double getJVMTimeInSec() {
        return System.nanoTime() / 1_000_000_000.0;
    }
}
