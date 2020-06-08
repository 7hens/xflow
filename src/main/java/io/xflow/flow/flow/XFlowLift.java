package io.xflow.flow.flow;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import io.xflow.flow.caller.Collector;

@ApiStatus.Internal
class XFlowLift<T, R> extends XFlow<R> {
    private final XFlow<T> upFlow;
    private final Operator<T, R> operator;

    XFlowLift(@NotNull XFlow<T> upFlow, @NotNull Operator<T, R> operator) {
        this.upFlow = upFlow;
        this.operator = operator;
    }

    @Override
    public void collect(@NotNull Collector<R> collector) {
        try {
            upFlow.collect(operator.apply(collector));
        } catch (Throwable e) {
            collector.terminate(e);
        }
    }
}
