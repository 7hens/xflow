package io.xflow.scheduler;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.xflow.cancellable.CompositeCancellable;
import io.xflow.func.Cancellable;

/**
 * @author 7hens
 */
class ExecutorScheduler extends Scheduler {
    private final Executor executor;

    ExecutorScheduler(Executor executor) {
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

    private static volatile Scheduler scheduledHelper;

    private static Scheduler getScheduledHelper() {
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
