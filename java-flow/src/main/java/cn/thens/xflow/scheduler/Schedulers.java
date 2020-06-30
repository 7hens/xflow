package cn.thens.xflow.scheduler;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author 7hens
 */
public final class Schedulers {
    private Schedulers() {
    }

    private static volatile Scheduler scheduledHelper;

    private static Scheduler getScheduledHelper() {
        if (scheduledHelper == null) {
            synchronized (ExecutorScheduler.class) {
                if (scheduledHelper == null) {
                    scheduledHelper = from(Executors.newScheduledThreadPool(1));
                }
            }
        }
        return scheduledHelper;
    }

    public static Scheduler from(final Executor executor) {
        if (executor instanceof ScheduledExecutorService) {
            return new ScheduledExecutorScheduler((ScheduledExecutorService) executor);
        }
        return new ExecutorScheduler(getScheduledHelper(), executor);
    }

    private static Scheduler newScheduler(String name, int expectedThreadCount) {
        int processorCount = Runtime.getRuntime().availableProcessors();
        int maxThreadCount = Math.max(processorCount, expectedThreadCount);
        return from(new ThreadPoolExecutor(processorCount, maxThreadCount,
                60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(1024),
                new SchedulerThreadFactory(name)));
    }

    private static volatile Scheduler IO;

    public static Scheduler io() {
        if (IO == null) {
            synchronized (ExecutorScheduler.class) {
                if (IO == null) {
                    IO = newScheduler("io", 64);
                }
            }
        }
        return IO;
    }

    private static volatile Scheduler CORE;

    public static Scheduler core() {
        if (CORE == null) {
            synchronized (ExecutorScheduler.class) {
                if (CORE == null) {
                    CORE = newScheduler("core", 2);
                }
            }
        }
        return CORE;
    }

    private static Scheduler UNCONFINED = new UnconfinedScheduler();

    public static Scheduler unconfined() {
        return UNCONFINED;
    }
}
