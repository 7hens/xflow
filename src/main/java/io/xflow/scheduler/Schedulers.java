package io.xflow.scheduler;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.xflow.cancellable.CompositeCancellable;
import io.xflow.func.Cancellable;

/**
 * @author 7hens
 */
public final class Schedulers {
    private Schedulers() {
    }

    public static Scheduler from(Executor executor) {
        return new ExecutorScheduler(executor);
    }

    private static volatile Scheduler IO;

    public static Scheduler io() {
        if (IO == null) {
            synchronized (ExecutorScheduler.class) {
                if (IO == null) {
                    ThreadPoolExecutor executor = new ThreadPoolExecutor(16, 32,
                            10, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(100),
                            new RejectedExecutionHandler() {
                                @Override
                                public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                                }
                            });
                    executor.allowsCoreThreadTimeOut();
                    IO = from(executor);
                }
            }
        }
        return IO;
    }

    public static Scheduler unconfined() {
        return new Scheduler() {
            @Override
            public Cancellable schedule(Runnable runnable, long delay, TimeUnit unit) {
                if (delay > 0) {
                    try {
                        Thread.sleep(unit.toMillis(delay));
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                runnable.run();
                return new CompositeCancellable();
            }
        };
    }
}
