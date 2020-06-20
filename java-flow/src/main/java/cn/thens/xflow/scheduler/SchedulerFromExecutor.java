package cn.thens.xflow.scheduler;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import cn.thens.xflow.cancellable.Cancellable;
import cn.thens.xflow.cancellable.CompositeCancellable;

/**
 * @author 7hens
 */
class SchedulerFromExecutor extends Scheduler {
    private static volatile Scheduler IO;

    public static Scheduler io() {
        if (IO == null) {
            synchronized (SchedulerFromExecutor.class) {
                if (IO == null) {
                    ThreadPoolExecutor executor = new ThreadPoolExecutor(16, 32,
                            10, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(100),
                            new ThreadFactory() {
                                @Override
                                public Thread newThread(Runnable runnable) {
                                    return new Thread(runnable);
                                }
                            },
                            new RejectedExecutionHandler() {
                                @Override
                                public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                                    throw new RuntimeException();
                                }
                            });
                    executor.allowsCoreThreadTimeOut();
                    IO = new SchedulerFromExecutor(executor);
                }
            }
        }
        return IO;
    }

    private final Executor executor;

    public SchedulerFromExecutor(Executor executor) {
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
            synchronized (SchedulerFromExecutor.class) {
                if (scheduledHelper == null) {
                    scheduledHelper = new SchedulerFromExecutor(Executors.newSingleThreadScheduledExecutor());
                }
            }
        }
        return scheduledHelper;
    }
}
