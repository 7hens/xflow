package io.xflow.flow.caller;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;

import io.xflow.cancellable.CompositeCancellable;
import io.xflow.flow.reply.Reply;
import io.xflow.func.Action;
import io.xflow.func.Functions;
import io.xflow.scheduler.Scheduler;

/**
 * @author 7hens
 */
public class CallerEmitter<T> extends CompositeCancellable implements Caller<T>, Emitter<T> {
    private final AtomicBoolean isTerminated = new AtomicBoolean(false);
    private final Scheduler scheduler;
    private final Collector<T> collector;
    private final Queue<Action> actionQueue = new LinkedList<>();

    public CallerEmitter(Scheduler scheduler, Collector<T> collector) {
        this.scheduler = scheduler;
        this.collector = collector;
    }

    @Override
    public void receive(@NotNull Reply<T> reply) {
        try {
            if (reply.over()) {
                over(reply.error(), Functions.emptyAction());
                return;
            }
            emit(reply.value(), () -> {
                if (!isTerminated.get()) {
                    reply.callee().reply(this);
                }
            });
        } catch (Throwable e) {
            over(e, Functions.emptyAction());
        }
    }

    @Override
    public void emit(T value, Action action) {
        handle(() -> {
            try {
                collector.onEach(value, action);
            } catch (Throwable e) {
                over(e, Functions.emptyAction());
            }
        });
    }

    @Override
    public void over(@Nullable Throwable e, Action action) {
        handle(() -> {
            if (isTerminated.compareAndSet(false, true)) {
                if (e == null) {
                    collector.onTerminate(null, action);
                } else {
                    collector.onTerminate(e, action);
                }
                actionQueue.clear();
            }
        });
    }

    private void handle(Action action) {
        if (isTerminated.get()) return;
        actionQueue.add(action);
        add(scheduler.schedule(() -> {
            while (!actionQueue.isEmpty() && !isTerminated.get()) {
                //noinspection ConstantConditions
                actionQueue.poll().run();
            }
        }));
    }

    @Override
    protected void onCancel() {
        super.onCancel();
        over(new CancellationException(), Functions.emptyAction());
    }

    public Collector<T> collector() {
        CallerEmitter<T> emitter = this;
        return new Collector<T>() {
            @Override
            public void onEach(T t, Action action) {
                emitter.emit(t, action);
            }

            @Override
            public void onTerminate(@Nullable Throwable e, Action action) {
                emitter.over(e, action);
            }
        };
    }
}
