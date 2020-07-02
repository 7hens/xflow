package cn.thens.xflow.scheduler;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import cn.thens.xflow.func.Lazy;

/**
 * @author 7hens
 */
public final class Schedulers {
    private Schedulers() {
    }

    private static Lazy<Scheduler> SINGLE = Lazy.of(() -> {
        return from(Executors.newScheduledThreadPool(1, runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("single");
            thread.setDaemon(true);
            return thread;
        }));
    });

    public static Scheduler single() {
        return SINGLE.get();
    }

    public static Scheduler from(final Executor executor) {
        if (executor instanceof ScheduledExecutorService) {
            return new ScheduledExecutorScheduler((ScheduledExecutorService) executor);
        }
        return new ExecutorScheduler(single(), executor);
    }

    private static Lazy<Scheduler> IO = Lazy.of(() -> newScheduler("io", 64));

    public static Scheduler io() {
        return IO.get();
    }

    private static Lazy<Scheduler> CORE = Lazy.of(() -> newScheduler("core", 2));

    public static Scheduler core() {
        return CORE.get();
    }

    private static Scheduler newScheduler(String name, int expectedThreadCount) {
        int processorCount = Runtime.getRuntime().availableProcessors();
        int maxThreadCount = Math.max(processorCount, expectedThreadCount);
        return from(new ThreadPoolExecutor(processorCount, maxThreadCount,
                60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(1024),
                new SchedulerThreadFactory(name)));
    }
}
