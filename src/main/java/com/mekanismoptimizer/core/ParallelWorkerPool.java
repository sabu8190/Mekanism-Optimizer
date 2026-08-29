package com.mekanismoptimizer.core;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public final class ParallelWorkerPool {
    private static final int THREAD_COUNT = Math.max(2, Runtime.getRuntime().availableProcessors() - 1);
    private static final AtomicInteger THREAD_ID = new AtomicInteger(1);
    
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(THREAD_COUNT, r -> {
        Thread t = new Thread(r, "MekanismOptimizer-Worker-" + THREAD_ID.getAndIncrement());
        t.setDaemon(true);
        t.setPriority(Thread.NORM_PRIORITY - 1);
        return t;
    });

    private static final ForkJoinPool FORK_JOIN_POOL = new ForkJoinPool(
        THREAD_COUNT,
        ForkJoinPool.defaultForkJoinWorkerThreadFactory,
        null,
        true
    );

    private ParallelWorkerPool() {
    }

    public static void execute(Runnable task) {
        EXECUTOR.execute(() -> {
            try {
                task.run();
                MekanismOptimizerLogger.recordParallelTask();
            } catch (Throwable t) {
                MekanismOptimizerLogger.error("Error in parallel worker task", t);
            }
        });
    }

    public static <T> Future<T> submit(Callable<T> task) {
        MekanismOptimizerLogger.recordParallelTask();
        return EXECUTOR.submit(task);
    }

    public static ForkJoinPool getForkJoinPool() {
        return FORK_JOIN_POOL;
    }

    public static int getThreadCount() {
        return THREAD_COUNT;
    }
}