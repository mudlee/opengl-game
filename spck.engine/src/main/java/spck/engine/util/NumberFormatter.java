package spck.engine.util;

import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

// From: https://stackoverflow.com/questions/4753251/how-to-go-about-formatting-1200-to-1-2k-in-java
public class NumberFormatter {
    private static final NavigableMap<Long, String> simpleSuffixes = new TreeMap<>();
    private static final NavigableMap<Long, String> binaryUnitSuffixes = new TreeMap<>();

    static {
        simpleSuffixes.put(1_000L, "k");
        simpleSuffixes.put(1_000_000L, "M");
        simpleSuffixes.put(1_000_000_000L, "G");
        simpleSuffixes.put(1_000_000_000_000L, "T");
        simpleSuffixes.put(1_000_000_000_000_000L, "P");
        simpleSuffixes.put(1_000_000_000_000_000_000L, "E");

        binaryUnitSuffixes.put(1024L, "KiB");
        binaryUnitSuffixes.put(1048576L, "MiB");
        binaryUnitSuffixes.put(1073741824L, "GiB");
        binaryUnitSuffixes.put(1099511627776L, "TiB");
    }

    public static String formatSimple(long value) {
        //Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
        if (value == Long.MIN_VALUE) return formatSimple(Long.MIN_VALUE + 1);
        if (value < 0) return "-" + formatSimple(-value);
        if (value < 1000) return Long.toString(value); //deal with easy case

        Map.Entry<Long, String> e = simpleSuffixes.floorEntry(value);
        Long divideBy = e.getKey();
        String suffix = e.getValue();

        long truncated = value / (divideBy / 10); //the number part of the output times 10
        boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
        return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
    }

    public static String formatBinaryUnit(long bytes) {
        //Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
        if (bytes == Long.MIN_VALUE) return formatBinaryUnit(Long.MIN_VALUE + 1);
        if (bytes < 0) return "-" + formatBinaryUnit(-bytes);
        if (bytes < 1024) return bytes + " Bytes"; //deal with easy case

        Map.Entry<Long, String> e = binaryUnitSuffixes.floorEntry(bytes);
        Long divideBy = e.getKey();
        String suffix = e.getValue();

        long truncated = bytes / (divideBy / 10); //the number part of the output times 10
        boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
        return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
    }
}
