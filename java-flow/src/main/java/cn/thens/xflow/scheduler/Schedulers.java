package cn.thens.xflow.scheduler;

import java.util.concurrent.Executor;

/**
 * @author 7hens
 */
public final class Schedulers {
    private Schedulers() {
    }

    public static Scheduler from(final Executor executor) {
        return new ExecutorScheduler(executor);
    }

    private static volatile Scheduler IO;

    public static Scheduler io() {
        if (IO == null) {
            synchronized (ExecutorScheduler.class) {
                if (IO == null) {
                    IO = ExecutorScheduler.newScheduler("io", 64);
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
                    CORE = ExecutorScheduler.newScheduler("core", 2);
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
