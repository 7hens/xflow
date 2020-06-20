package cn.thens.xflow.scheduler;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import cn.thens.xflow.cancellable.Cancellable;
import cn.thens.xflow.cancellable.CompositeCancellable;

/**
 * @author 7hens
 */
public abstract class Scheduler {
    public abstract Cancellable schedule(Runnable runnable, long delay, TimeUnit unit);

    public Cancellable schedule(Runnable runnable) {
        return schedule(runnable, 0L, TimeUnit.NANOSECONDS);
    }

    public Cancellable schedulePeriodically(final Runnable runnable, final long initialDelay,
                                            final long period, final TimeUnit unit) {
        final CompositeCancellable cancellable = new CompositeCancellable();
        cancellable.addCancellable(schedule(new Runnable() {
            @Override
            public void run() {
                if (!cancellable.isCancelled()) {
                    runnable.run();
                    cancellable.addCancellable(schedule(this, period, unit));
                }
            }
        }, initialDelay, unit));
        return cancellable;
    }

    public CancellableScheduler cancellable() {
        return new CancellableScheduler(this);
    }

    public static Scheduler from(final Executor executor) {
        return new SchedulerFromExecutor(executor);
    }

    public static Scheduler io() {
        return SchedulerFromExecutor.io();
    }


}
