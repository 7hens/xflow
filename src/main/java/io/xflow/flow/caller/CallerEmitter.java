package io.xflow.flow.caller;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import io.xflow.cancellable.CompositeCancellable;
import io.xflow.flow.reply.Reply;
import io.xflow.scheduler.Scheduler;

/**
 * @author 7hens
 */
public class CallerEmitter<T> extends CompositeCancellable implements Caller<T>, Emitter<T> {
    private final AtomicBoolean isTerminated = new AtomicBoolean(false);
    private final AtomicInteger restCount = new AtomicInteger(1);
    private final Scheduler scheduler;
    private final Collector<T> collector;

    public CallerEmitter(Scheduler scheduler, Collector<T> collector) {
        this.scheduler = scheduler;
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
        restCount.getAndIncrement();
        scheduler.schedule(() -> {
            if (isTerminated.get()) return;
            try {
                collector.onCollect(value);
                if (restCount.decrementAndGet() <= 0) {
                    over(null);
                }
            } catch (Throwable e) {
                over(e);
            }
        });
    }

    @Override
    public void over(@Nullable Throwable e) {
        if (isTerminated.get()) return;
        scheduler.schedule(() -> {
            if (isTerminated.get()) return;
            if (e == null) {
                if (restCount.decrementAndGet() <= 0 && isTerminated.compareAndSet(false, true)) {
                    collector.onTerminate(null);
                }
            } else if (isTerminated.compareAndSet(false, true)) {
                collector.onTerminate(e);
            }
        });
    }

    @Override
    protected void onCancel() {
        super.onCancel();
        over(new CancellationException());
//        throw new RuntimeException();
    }

    public Collector<T>collector() {
        CallerEmitter<T> emitter = this;
        return new Collector<T>() {
            @Override
            public void onCollect(T t) {
                emitter.emit(t);
            }

            @Override
            public void onTerminate(@Nullable Throwable e) {
                emitter.over(e);
            }
        };
    }
}
