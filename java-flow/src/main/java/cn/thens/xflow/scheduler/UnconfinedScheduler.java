package cn.thens.xflow.scheduler;

import java.util.concurrent.TimeUnit;

import cn.thens.xflow.cancellable.Cancellable;
import cn.thens.xflow.cancellable.CompositeCancellable;

/**
 * @author 7hens
 */
public class UnconfinedScheduler extends Scheduler {
    @Override
    public Cancellable schedule(Runnable runnable, long delay, TimeUnit unit) {
        if (delay == 0) {
            runnable.run();
        } else {
            try {
                Thread.sleep(unit.toMillis(delay), (int)(unit.toNanos(delay) % 1000000));
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        return new CompositeCancellable();
    }
}
