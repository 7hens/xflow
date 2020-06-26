package cn.thens.xflow.flow;


import cn.thens.xflow.cancellable.Cancellable;
import cn.thens.xflow.scheduler.Scheduler;

/**
 * @author 7hens
 */
abstract class AbstractPolyFlow<T> extends PolyFlow<T> {
    @Override
    protected Cancellable collect(Scheduler scheduler, Collector<Flow<T>> collector) {
        CollectorEmitter<Flow<T>> emitter = new CollectorEmitter<>(scheduler, collector);
        emitter.scheduler().schedule(new Runnable() {
            @Override
            public void run() {
                try {
                    onStart(emitter);
                } catch (Throwable e) {
                    emitter.error(e);
                }
            }
        });
        return emitter;
    }

    protected abstract void onStart(CollectorEmitter<Flow<T>> emitter) throws Throwable;
}
