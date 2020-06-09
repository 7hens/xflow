package io.xflow.flow.caller;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;

import io.xflow.cancellable.CompositeCancellable;
import io.xflow.flow.reply.Reply;

/**
 * @author 7hens
 */
public abstract class CallerEmitter<T> extends CompositeCancellable implements Caller<T>, Emitter<T> {
    private final AtomicBoolean isTerminated = new AtomicBoolean(false);

    protected abstract Collector<T> baseCollector();

    @Override
    public void receive(@NotNull Reply<T> reply) {
        if (reply.over()) {
            over(reply.error());
            return;
        }
        try {
            emit(reply.value());
            if (!isTerminated.get()) {
                reply.callee().reply(this);
            }
        } catch (Throwable e) {
            over(e);
        }
    }

    @Override
    public void emit(T value) {
        if (isTerminated.get()) return;
        baseCollector().onCollect(value);
    }

    @Override
    public void over(@Nullable Throwable e) {
        if (isTerminated.compareAndSet(false, true)) {
            baseCollector().onTerminate(e);
        }
    }

    @Override
    protected void onCancel() {
        super.onCancel();
        over(new CancellationException());
//        throw new RuntimeException();
    }

    public static <T> CallerEmitter<T> of(Collector<T> collector) {
        return new CallerEmitter<T>() {
            @Override
            protected Collector<T> baseCollector() {
                return collector;
            }
        };
    }
}
