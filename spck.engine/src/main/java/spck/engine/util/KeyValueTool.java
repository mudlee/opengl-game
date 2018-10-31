package spck.engine.util;

import java.util.HashMap;
import java.util.Map;

public class KeyValueTool {
    private static Map<String, Object> storage = new HashMap<>();

    public static void computeIfValueChanged(String key, Object value, Runnable runnable) {
        if (storage.containsKey(key) && storage.get(key).equals(value)) {
            return;
        }

        storage.put(key, value);
        runnable.run();
    }
}