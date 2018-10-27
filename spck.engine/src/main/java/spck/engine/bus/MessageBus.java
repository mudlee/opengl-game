package spck.engine.bus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class MessageBus {
    private static final List<Consumer> CONSUMER_EMPTY_LIST = new ArrayList<>();
    private static final List<Runnable> RUNNABLE_EMPTY_LIST = new ArrayList<>();
    private static final Map<String, List<Consumer>> consumers = new HashMap<>();
    private static final Map<String, List<Runnable>> runnables = new HashMap<>();

    public static void broadcast(String key, Object event) {
        for (Consumer consumer : consumers.getOrDefault(key, CONSUMER_EMPTY_LIST)) {
            //noinspection unchecked
            consumer.accept(event);
        }

        for (Runnable runnable : runnables.getOrDefault(key, RUNNABLE_EMPTY_LIST)) {
            //noinspection unchecked
            runnable.run();
        }
    }

    public static void broadcast(String key) {
        for (Consumer consumer : consumers.getOrDefault(key, CONSUMER_EMPTY_LIST)) {
            //noinspection unchecked
            consumer.accept(null);
        }

        for (Runnable runnable : runnables.getOrDefault(key, RUNNABLE_EMPTY_LIST)) {
            //noinspection unchecked
            runnable.run();
        }
    }

    public static void register(String key, Consumer consumer) {
        consumers.putIfAbsent(key, new ArrayList<>());
        consumers.get(key).add(consumer);
    }

    public static void register(String key, Runnable runnable) {
        runnables.putIfAbsent(key, new ArrayList<>());
        runnables.get(key).add(runnable);
    }
}
