package io.xflow.flow.caller;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;

import io.xflow.flow.reply.Reply;

/**
 * @author 7hens
 */
public abstract class Collector<T> implements Caller<T> {
    private final AtomicBoolean isTerminated = new AtomicBoolean(false);

    @Override
    public void receive(@NotNull Reply<T> reply) {
        if (reply.over()) {
            terminate(reply.error());
            return;
        }
        try {
            collect(reply.value());
            if (!isTerminated.get()) {
                reply.callee().reply(this);
            }
        } catch (Throwable e) {
            terminate(e);
        }
    }

    public void collect(T value) {
        if (isTerminated.get()) return;
        onCollect(value);
    }

    public void terminate(@Nullable Throwable e) {
        if (isTerminated.compareAndSet(false, true)) {
            onTerminate(e);
        }
    }

    protected abstract void onCollect(T t);

    protected abstract void onTerminate(Throwable e);
}
