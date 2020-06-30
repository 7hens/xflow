package cn.thens.xflow.scheduler;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import cn.thens.xflow.cancellable.CompositeCancellable;

/**
 * @author 7hens
 */
final class CancellableFuture<V> extends CompositeCancellable implements Future<V> {
    private final Future<V> future;

    CancellableFuture(Future<V> future) {
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
