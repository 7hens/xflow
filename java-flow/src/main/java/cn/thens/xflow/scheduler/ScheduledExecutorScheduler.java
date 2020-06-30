package cn.thens.xflow.scheduler;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import cn.thens.xflow.cancellable.Cancellable;
import cn.thens.xflow.cancellable.CompositeCancellable;

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
        return new CancellableFuture<>(executor.submit(runnable));
    }

    @Override
    public Cancellable schedule(Runnable runnable, long delay, TimeUnit unit) {
        return new CancellableFuture<>(executor.schedule(runnable, delay, unit));
    }

    @Override
    public Cancellable schedulePeriodically(Runnable runnable, long initialDelay, long period, TimeUnit unit) {
        return new CancellableFuture<>(executor.scheduleAtFixedRate(runnable, initialDelay, period, unit));
    }

    private static final class CancellableFuture<V> extends CompositeCancellable implements Future<V> {
        private final Future<V> future;

        private CancellableFuture(Future<V> future) {
            this.future = future;
        }

        @Override
        public boolean cancel(boolean b) {
            return future.cancel(b);
        }

        @Override
        public boolean isDone() {
            return future.isDone();
        }

        @Override
        public V get() throws InterruptedException, ExecutionException {
            return future.get();
        }

        @Override
        public V get(long l, @NotNull TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
            return future.get(l, timeUnit);
        }

        @Override
        protected void onCancel() {
            super.onCancel();
            cancel(false);
        }
    }
}
