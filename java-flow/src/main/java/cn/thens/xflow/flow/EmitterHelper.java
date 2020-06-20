package cn.thens.xflow.flow;

import cn.thens.xflow.cancellable.Cancellable;
import cn.thens.xflow.scheduler.Scheduler;

/**
 * @author 7hens
 */
class EmitterHelper<T> implements Emitter<T> {
    private final Emitter<T> emitter;

    EmitterHelper(Emitter<T> emitter) {
        this.emitter = emitter;
    }

    @Override
    public void emit(Reply<T> reply) {
        emitter.emit(reply);
    }

    @Override
    public void data(T data) {
        emitter.data(data);
    }

    @Override
    public void error(Throwable error) {
        emitter.error(error);
    }

    @Override
    public void complete() {
        emitter.complete();
    }

    @Override
    public boolean isTerminated() {
        return emitter.isTerminated();
    }

    @Override
    public void addCancellable(Cancellable cancellable) {
        emitter.addCancellable(cancellable);
    }

    @Override
    public Scheduler scheduler() {
        return emitter.scheduler();
    }

    public Collector<T> collector() {
        return new Collector<T>() {
            @Override
            public void onCollect(Reply<T> reply) {
                emit(reply);
            }
        };
    }
}
