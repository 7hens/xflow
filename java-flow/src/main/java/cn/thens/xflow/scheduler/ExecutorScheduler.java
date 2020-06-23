package cn.thens.xflow.scheduler;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import cn.thens.xflow.cancellable.Cancellable;
import cn.thens.xflow.cancellable.CompositeCancellable;

/**
 * @author 7hens
 */
class ExecutorScheduler extends Scheduler {

    private final Executor executor;

    public ExecutorScheduler(Executor executor) {
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

    static class MyThreadFactory implements ThreadFactory {
        private final AtomicLong number = new AtomicLong();
        final String prefix;
        final int priority;

        MyThreadFactory(String prefix, int priority) {
            this.prefix = prefix;
            this.priority = priority;
        }

        MyThreadFactory(String prefix) {
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

    static ExecutorScheduler newScheduler(String name, int expectedThreadCount) {
        int processorCount = Runtime.getRuntime().availableProcessors();
        int maxThreadCount = Math.max(processorCount, expectedThreadCount);
        return new ExecutorScheduler(new ThreadPoolExecutor(processorCount, maxThreadCount,
                60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(1024),
                new ExecutorScheduler.MyThreadFactory(name)));
    }
}
