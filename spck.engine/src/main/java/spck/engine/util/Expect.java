package spck.engine.util;

public class Expect {
    public static <T> T notNull(T object, String message) {
        if (object == null) {
            throw new RuntimeException(message);
        }
        return object;
    }
}
