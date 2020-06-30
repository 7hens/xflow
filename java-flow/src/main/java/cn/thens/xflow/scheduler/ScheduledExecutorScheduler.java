package cn.thens.xflow.scheduler;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import cn.thens.xflow.cancellable.Cancellable;

/**
 * @author 7hens
 */
class ScheduledExecutorScheduler extends Scheduler {
    private final ScheduledExecutorService executor;

    ScheduledExecutorScheduler(ScheduledExecutorService executor) {
        this.executor = executor;
    }

    @Override
    public Cancellable schedule(Runnable runnable) {
        return cancellable(executor.submit(runnable));
    }

    @Override
    public Cancellable schedule(Runnable runnable, long delay, TimeUnit unit) {
        return cancellable(executor.schedule(runnable, delay, unit));
    }

    @Override
    public Cancellable schedulePeriodically(Runnable runnable, long initialDelay, long period, TimeUnit unit) {
        return cancellable(executor.scheduleAtFixedRate(runnable, initialDelay, period, unit));
    }

    private <V> CancellableFuture<V> cancellable(Future<V> future) {
        return new CancellableFuture<>(future);
    }
}
