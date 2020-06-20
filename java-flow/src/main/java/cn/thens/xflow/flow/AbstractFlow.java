package cn.thens.xflow.flow;


import cn.thens.xflow.cancellable.Cancellable;
import cn.thens.xflow.scheduler.Scheduler;

/**
 * @author 7hens
 */
abstract class AbstractFlow<T> extends Flow<T> {
    @Override
    protected Cancellable collect(Collector<T> collector, Scheduler scheduler) {
        CollectorEmitter<T> emitter = new CollectorEmitter<T>(collector, scheduler);
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

    protected abstract void onStart(CollectorEmitter<T> emitter) throws Throwable;
}
