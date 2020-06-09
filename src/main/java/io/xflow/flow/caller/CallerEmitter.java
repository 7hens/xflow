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
public class CallerEmitter<T> extends CompositeCancellable implements Caller<T>, Emitter<T> {
    private final AtomicBoolean isTerminated = new AtomicBoolean(false);
    private final Collector<T> collector;

    public CallerEmitter(Collector<T> collector) {
        this.collector = collector;
    }

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
        collector.onCollect(value);
    }

    @Override
    public void over(@Nullable Throwable e) {
        if (isTerminated.compareAndSet(false, true)) {
            collector.onTerminate(e);
        }
    }

    @Override
    protected void onCancel() {
        super.onCancel();
        over(new CancellationException());
//        throw new RuntimeException();
    }
}
