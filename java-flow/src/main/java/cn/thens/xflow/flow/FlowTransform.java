package cn.thens.xflow.flow;

import cn.thens.xflow.cancellable.Cancellable;
import cn.thens.xflow.scheduler.Scheduler;

/**
 * @author 7hens
 */
class FlowTransform<T, R> extends Flow<R> {
    private final Flow<T> upFlow;
    private final Operator<T, R> operator;

    FlowTransform(Flow<T> upFlow, Operator<T, R> operator) {
        this.upFlow = upFlow;
        this.operator = operator;
    }

    @Override
    protected Cancellable collect(Scheduler scheduler, Collector<R> collector) {
        CollectorEmitter<R> emitter = new CollectorEmitter<>(scheduler, collector);
        try {
            upFlow.collect(emitter, operator.apply(emitter));
        } catch (Throwable e) {
            collector.onCollect(Reply.error(e));
        }
        return emitter;
    }
}
