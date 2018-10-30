package spck.engine.util;

import spck.engine.OS;

public class OSNameParser {
    public static OS parse(String osName) {
        if (osName.startsWith("Mac")) {
            return OS.MACOS;
        } else if (osName.startsWith("Windows")) {
            return OS.WINDOWS;
        } else if (osName.startsWith("Linux")) {
            return OS.LINUX;
        } else {
            throw new RuntimeException(String.format("Unsupported OS: %s", osName));
        }
    }
}
