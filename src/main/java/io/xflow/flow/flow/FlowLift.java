package io.xflow.flow.flow;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import io.xflow.flow.caller.CallerEmitter;
import io.xflow.flow.caller.Collector;
import io.xflow.func.Cancellable;

@ApiStatus.Internal
class FlowLift<T, R> extends Flow<R> {
    private final Flow<T> upFlow;
    private final Operator<T, R> operator;

    FlowLift(@NotNull Flow<T> upFlow, @NotNull Operator<T, R> operator) {
        this.upFlow = upFlow;
        this.operator = operator;
    }

    @Override
    public Cancellable collect(@NotNull Collector<R> collector) {
        try {
            CallerEmitter<R> emitter = new CallerEmitter<>(collector);
            emitter.add(upFlow.collect(operator.apply(emitter)));
            return emitter;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
