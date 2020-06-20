package cn.thens.xflow.scheduler;


import java.util.concurrent.TimeUnit;

import cn.thens.xflow.cancellable.Cancellable;
import cn.thens.xflow.cancellable.CompositeCancellable;

/**
 * @author 7hens
 */
public class CancellableScheduler extends Scheduler implements Cancellable {
    private CompositeCancellable compositeCancellable = new CompositeCancellable();
    private final Scheduler scheduler;

    CancellableScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public Cancellable schedule(Runnable runnable, long delay, TimeUnit unit) {
        if (isCancelled()) {
            return this;
        }
        Cancellable cancellable = scheduler.schedule(runnable, delay, unit);
        compositeCancellable.addCancellable(cancellable);
        return cancellable;
    }

    @Override
    public void cancel() {
        compositeCancellable.cancel();
    }

    @Override
    public boolean isCancelled() {
        return compositeCancellable.isCancelled();
    }
}
