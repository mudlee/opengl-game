package spck.engine.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class RunOnce {
    private static final Logger LOGGER = LoggerFactory.getLogger(RunOnce.class);
    private final static Set<String> ran = new HashSet<>();

    public static void run(String id, Runnable runnable) {
        if (ran.contains(id)) {
            return;
        }

        ran.add(id);
        LOGGER.debug("Running {}...", id);
        runnable.run();
    }
}
