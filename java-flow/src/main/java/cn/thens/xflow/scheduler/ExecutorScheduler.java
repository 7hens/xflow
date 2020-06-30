package cn.thens.xflow.scheduler;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import cn.thens.xflow.cancellable.Cancellable;

/**
 * @author 7hens
 */
class ExecutorScheduler extends Scheduler {
    private final Scheduler scheduledHelper;
    private final Executor executor;

    ExecutorScheduler(Scheduler scheduledHelper, Executor executor) {
        this.scheduledHelper = scheduledHelper;
        this.executor = executor;
    }

    @Override
    public Cancellable schedule(final Runnable runnable, long delay, TimeUnit unit) {
        return scheduledHelper.schedule(() -> executor.execute(runnable), delay, unit);
    }

    @Override
    public Cancellable schedulePeriodically(Runnable runnable, long initialDelay, long period, TimeUnit unit) {
        return scheduledHelper.schedulePeriodically(() -> executor.execute(runnable), initialDelay, period, unit);
    }
}
