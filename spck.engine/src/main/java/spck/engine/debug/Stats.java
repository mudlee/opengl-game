package spck.engine.debug;

public class Stats {
    public static int numOfVerts = 0;
    public static int numOfTotalVerts = 0;
    public static int numOfBatchGroups = 0;
    public static int numOfBatches = 0;

    public static void reset() {
        numOfVerts = 0;
        numOfTotalVerts = 0;
        numOfBatchGroups = 0;
        numOfBatches = 0;
    }
}
