package com.example.demo;

import com.example.demo.utils.PerformanceUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CallbackSandbox {
    private static final Logger logger = LoggerFactory.getLogger(CallbackSandbox.class);

    @Test
    void testGenerateReportTime() {
        PerformanceUtils.measureRuntime(this::mainSync, "Synchronous test");
    }

    @Test
    public void mainSync() {

        int b = getBase();
        int l1 = getTransformL1(b);
        int l2 = getTransformL2(l1);
        int l3 = getTransformL3(l2);
        double l4 = getTransformL4(l3);
        String result = getTransformL5(l4);
        logger.warn("result: " + result);
    }

    @Test
    void testGenerateReportTimeAsync() {
        PerformanceUtils.measureRuntime(this::mainAsync, "Asynchronous test");
    }

    public void mainAsync() {
        logger.warn("start");
        int maxThread = 100;
        logger.warn("max threads: " + maxThread);
        ExecutorService executorService = Executors.newFixedThreadPool(maxThread);
        try {
            List<CompletableFuture<String>> futures = new ArrayList<>();
            int n = 10;
            while (n-- > 0) {
                futures.add(getResultAsync(executorService));
            }

            CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
                    .thenAccept(x -> {
                        logger.warn("all awaited, n: " + futures.size());
                    }).join();

            List<String> result = futures.stream()
                    .map(CompletableFuture::join)
                    .toList();

            logger.warn("result async, n: " + result.size());
            result.forEach(logger::info);
            logger.warn("result sum: " + result.stream().mapToDouble(Double::parseDouble).sum());
        } finally {
            executorService.shutdown();
        }
    }

    CompletableFuture<String> getResultAsync(ExecutorService executorService) {
        return CompletableFuture.supplyAsync(() -> getBase(), executorService)
                .thenApply(i -> getTransformL1(i))
                .thenApply(i -> getTransformL2(i))
                .thenApply(i -> getTransformL3(i))
                .thenApply(i -> getTransformL4(i))
                .thenApply(d -> getTransformL5(d));
    }

    int getBase()  {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        logger.warn("base");
        return 10;
    }

    int getTransformL1(int input)  {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        logger.warn("l1");
        return input * 100;
    }

    int getTransformL2(int input)  {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return input / 2;
    }

    int getTransformL3(int input)  {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return input * input;
    }

    double getTransformL4(int input)  {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return 0.999d * input;
    }

    String getTransformL5(double input)  {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return String.format("%.2f", input);
    }
}
