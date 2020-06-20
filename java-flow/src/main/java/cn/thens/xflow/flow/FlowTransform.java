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
    protected Cancellable collect(Collector<R> collector, Scheduler scheduler) {
        CollectorEmitter<R> emitter = new CollectorEmitter<>(collector, scheduler);
        try {
            emitter.addCancellable(upFlow.collect(operator.apply(emitter), scheduler));
        } catch (Throwable e) {
            collector.onCollect(Reply.error(e));
        }
        return emitter;
    }
}
