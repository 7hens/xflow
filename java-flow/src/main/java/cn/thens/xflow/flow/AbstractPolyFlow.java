package cn.thens.xflow.flow;


import cn.thens.xflow.cancellable.Cancellable;
import cn.thens.xflow.scheduler.Scheduler;

/**
 * @author 7hens
 */
abstract class AbstractPolyFlow<T> extends PolyFlow<T> {
    @Override
    protected Cancellable collect(Scheduler scheduler, Collector<? super Flowable<T>> collector) {
        CollectorEmitter<? super Flowable<T>> emitter = CollectorEmitter.create(scheduler, collector);
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

    protected abstract void onStart(CollectorEmitter<? super Flowable<T>> emitter) throws Throwable;
}
