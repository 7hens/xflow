package cn.thens.xflow.flow;


import cn.thens.xflow.cancellable.Cancellable;
import cn.thens.xflow.scheduler.Scheduler;

/**
 * @author 7hens
 */
class FlowFlowOn<T> extends Flow<T> {
    private final Flow<T> upFlow;
    private final Scheduler upScheduler;

    FlowFlowOn(Flow<T> upFlow, Scheduler upScheduler) {
        this.upFlow = upFlow;
        this.upScheduler = upScheduler;
    }

    @Override
    protected Cancellable collect(Scheduler scheduler, Collector<T> collector) {
        return upFlow.collect(upScheduler, CollectorEmitter.create(scheduler, collector));
    }
}
