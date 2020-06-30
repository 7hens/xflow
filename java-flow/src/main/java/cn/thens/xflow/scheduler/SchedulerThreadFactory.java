package cn.thens.xflow.scheduler;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author 7hens
 */
@SuppressWarnings("WeakerAccess")
class SchedulerThreadFactory implements ThreadFactory {
    private final AtomicLong number = new AtomicLong();
    private final String prefix;
    private final int priority;

    SchedulerThreadFactory(String prefix, int priority) {
        this.prefix = prefix;
        this.priority = priority;
    }

    SchedulerThreadFactory(String prefix) {
        this(prefix, Thread.NORM_PRIORITY);
    }

    @Override
    public Thread newThread(@NotNull Runnable runnable) {
        Thread thread = new Thread(runnable);
        thread.setName(prefix + "-" + number.incrementAndGet());
        thread.setPriority(priority);
        thread.setDaemon(true);
        return thread;
    }
}
