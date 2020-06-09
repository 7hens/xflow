package io.xflow.flow.flow;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import io.xflow.cancellable.CompositeCancellable;
import io.xflow.flow.caller.Collector;
import io.xflow.flow.caller.CallerEmitter;
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
        CompositeCancellable cancellable = new CompositeCancellable();
        try {
            cancellable.add(upFlow.collect(operator.apply(CallerEmitter.of(collector))));
        } catch (Throwable e) {
            collector.onTerminate(e);
        }
        return cancellable;
    }
}
