package io.rxflow.scheduler;

import java.util.concurrent.TimeUnit;

import io.rxflow.func.Cancellable;
import io.rxflow.cancellable.CompositeCancellable;

/**
 * @author 7hens
 */
public abstract class RxScheduler {
    public abstract Cancellable schedule(Runnable runnable, long delay, TimeUnit unit);

    public Cancellable schedule(Runnable runnable) {
        return schedule(runnable, 0L, TimeUnit.NANOSECONDS);
    }

    public Cancellable schedulePeriodically(final Runnable runnable, final long initialDelay,
                                            final long period, final TimeUnit unit) {
        final CompositeCancellable cancellable = new CompositeCancellable();
        return cancellable.add(schedule(new Runnable() {
            @Override
            public void run() {
                if (cancellable.isCancelled()) return;
                runnable.run();
                cancellable.add(schedule(this, period, unit));
            }
        }, initialDelay, unit));
    }

    public static RxScheduler io() {
        return ExecutorScheduler.io();
    }
}