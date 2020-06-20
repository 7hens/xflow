package cn.thens.xflow.flow;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import cn.thens.xflow.cancellable.Cancellable;
import cn.thens.xflow.scheduler.Scheduler;

/**
 * @author 7hens
 */
public class FlowEmitter<T> implements Emitter<T> {
    private List<Emitter<T>> emitters = new ArrayList<>();
    private CollectorEmitter<T> collectorEmitter = new CollectorEmitter<>(new Collector<T>() {
        @Override
        public void onCollect(Reply<T> reply) {
            for (Emitter<T> emitter : emitters) {
                emitter.emit(reply);
            }
        }
    }, Scheduler.io());

    @Override
    public void emit(Reply<T> reply) {
        collectorEmitter.emit(reply);
    }

    @Override
    public void data(T data) {
        collectorEmitter.data(data);
    }

    @Override
    public void error(Throwable error) {
        collectorEmitter.error(error);
    }

    @Override
    public void complete() {
        collectorEmitter.complete();
    }

    @Override
    public boolean isTerminated() {
        return collectorEmitter.isTerminated();
    }

    @Override
    public void addCancellable(Cancellable cancellable) {
        collectorEmitter.addCancellable(cancellable);
    }

    @Override
    public Scheduler scheduler() {
        return collectorEmitter.scheduler();
    }

    public Flow<T> asFlow() {
        final AtomicReference<Emitter<T>> emitterRef = new AtomicReference<>(null);
        return Flow.<T>create(emitter -> {
            emitterRef.set(emitter);
            emitters.add(emitter);
        })////////////
                .onCollect(new CollectorHelper<T>() {
                    @Override
                    protected void onTerminate(Throwable e) {
                        super.onTerminate(e);
                        Emitter<T> emitter = emitterRef.get();
                        if (emitter != null) {
                            emitters.remove(emitter);
                        }
                    }
                });
    }
}
