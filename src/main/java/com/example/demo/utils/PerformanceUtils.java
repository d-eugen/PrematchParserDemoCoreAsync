package com.example.demo.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.function.Supplier;

public class PerformanceUtils {
    private static final Logger logger = LoggerFactory.getLogger(PerformanceUtils.class);
    public static void measureRuntime(Runnable task, String taskName) {
        long startTime = System.nanoTime();

        try {
            task.run();
        } finally {
            long endTime = System.nanoTime();
            long duration = endTime - startTime;
            logger.debug(String.format("Execution time for %s: %d ms%n", taskName, duration / 1000000));
        }
    }

    public static <T> Optional<T> measureRuntime(Supplier<T> task, String taskName) {
        Optional<T> result = Optional.empty();
        long startTime = System.nanoTime();

        try {
            result = Optional.of(task.get());
        } finally {
            long endTime = System.nanoTime();
            long duration = endTime - startTime;
            logger.debug(String.format("Execution time for %s: %d ms%n", taskName, duration / 1000000));
        }
        return result;
    }
}
