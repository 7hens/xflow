package io.xflow.flow.flow;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import io.xflow.cancellable.CompositeCancellable;
import io.xflow.flow.caller.CallerEmitter;
import io.xflow.flow.caller.Collector;
import io.xflow.func.Cancellable;

@ApiStatus.Internal
class XFlowLift<T, R> extends XFlow<R> {
    private final XFlow<T> upFlow;
    private final Operator<T, R> operator;

    XFlowLift(@NotNull XFlow<T> upFlow, @NotNull Operator<T, R> operator) {
        this.upFlow = upFlow;
        this.operator = operator;
    }

    @Override
    public Cancellable collect(@NotNull Collector<R> collector) {
        try {
            CallerEmitter<R> emitter = CallerEmitter.of(collector);
            emitter.add(upFlow.collect(operator.apply(emitter)));
            return emitter;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
