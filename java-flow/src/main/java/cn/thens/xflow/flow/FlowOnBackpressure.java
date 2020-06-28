package cn.thens.xflow.flow;

import cn.thens.xflow.cancellable.Cancellable;
import cn.thens.xflow.scheduler.Scheduler;

class FlowOnBackpressure<T> extends Flow<T> {
    private final Flow<T> upFlow;
    private final Backpressure<T> backpressure;

    FlowOnBackpressure(Flow<T> upFlow, Backpressure<T> backpressure) {
        this.upFlow = upFlow;
        this.backpressure = backpressure;
    }

    @Override
    protected Cancellable collect(Scheduler scheduler, Collector<? super T> collector) {
        CollectorEmitter<T> emitter = CollectorEmitter.create(scheduler, collector, backpressure);
        upFlow.collect(emitter);
        return emitter;
    }
}
