package io.xflow.flow.flow;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executors;

import io.xflow.flow.caller.CallerEmitter;
import io.xflow.flow.caller.Collector;
import io.xflow.func.Cancellable;
import io.xflow.scheduler.Scheduler;
import io.xflow.scheduler.Schedulers;

@ApiStatus.Internal
class FlowLift<T, R> extends Flow<R> {
    private final Flow<T> upFlow;
    private final Operator<T, R> operator;

    FlowLift(@NotNull Flow<T> upFlow, @NotNull Operator<T, R> operator) {
        this.upFlow = upFlow;
        this.operator = operator;
    }

    @Override
    protected Cancellable collect(@NotNull Scheduler scheduler, @NotNull Collector<R> collector) {
        try {
            CallerEmitter<R> emitter = new CallerEmitter<>(scheduler, collector);
            emitter.add(upFlow.collect(scheduler, operator.apply(emitter)));
            return emitter;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static Scheduler scheduler(String name) {
        return Schedulers.from(Executors.newFixedThreadPool(8, runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName(name);
            return thread;
        }));
    }
}
