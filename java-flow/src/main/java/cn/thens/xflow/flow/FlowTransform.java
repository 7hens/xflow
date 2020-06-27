package cn.thens.xflow.flow;

import cn.thens.xflow.cancellable.Cancellable;
import cn.thens.xflow.scheduler.Scheduler;

/**
 * @author 7hens
 */
class FlowTransform<T, R> extends Flow<R> {
    private final Flow<T> upFlow;
    private final FlowOperator<? super T, ? extends R> operator;

    FlowTransform(Flow<T> upFlow, FlowOperator<? super T, ? extends R> operator) {
        this.upFlow = upFlow;
        this.operator = operator;
    }

    @Override
    protected Cancellable collect(Scheduler scheduler, Collector<? super R> collector) {
        CollectorEmitter<? super R> emitter = CollectorEmitter.create(scheduler, collector);
        try {
            upFlow.collect(emitter, operator.apply(emitter));
        } catch (Throwable e) {
            collector.onCollect(Reply.error(e));
        }
        return emitter;
    }
}
