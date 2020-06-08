package io.xflow.scheduler;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.xflow.func.Cancellable;
import io.xflow.cancellable.CompositeCancellable;

/**
 * @author 7hens
 */
class ExecutorScheduler extends RxScheduler {
    private static volatile RxScheduler IO;

    public static RxScheduler io() {
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
                    IO = new ExecutorScheduler(executor);
                }
            }
        }
        return IO;
    }

    private final Executor executor;

    private ExecutorScheduler(Executor executor) {
        this.executor = executor;
    }

    @Override
    public Cancellable schedule(final Runnable runnable, long delay, TimeUnit unit) {
        if (executor instanceof ScheduledExecutorService) {
            ScheduledExecutorService scheduledExecutor = (ScheduledExecutorService) this.executor;
            final ScheduledFuture<?> future = scheduledExecutor.schedule(runnable, delay, unit);
            return new CompositeCancellable() {
                @Override
                protected void onCancel() {
                    future.cancel(false);
                }
            };
        }
        return getScheduledHelper().schedule(new Runnable() {
            @Override
            public void run() {
                executor.execute(runnable);
            }
        }, delay, unit);
    }

    private static volatile RxScheduler scheduledHelper;

    private static RxScheduler getScheduledHelper() {
        if (scheduledHelper == null) {
            synchronized (ExecutorScheduler.class) {
                if (scheduledHelper == null) {
                    scheduledHelper = new ExecutorScheduler(Executors.newSingleThreadScheduledExecutor());
                }
            }
        }
        return scheduledHelper;
    }
}
