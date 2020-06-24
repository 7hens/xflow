package cn.thens.xflow.flow;


import cn.thens.xflow.cancellable.Cancellable;
import cn.thens.xflow.scheduler.Scheduler;

/**
 * @author 7hens
 */
abstract class AbstractFlow<T> extends Flow<T> {
    @Override
    protected Cancellable collect(Scheduler scheduler, Collector<T> collector) {
        CollectorEmitter<T> emitter = new CollectorEmitter<>(scheduler, collector);
        try {
            onStart(emitter);
        } catch (Throwable e) {
            emitter.error(e);
        }
        return emitter;
    }

    protected abstract void onStart(CollectorEmitter<T> emitter) throws Throwable;
}
