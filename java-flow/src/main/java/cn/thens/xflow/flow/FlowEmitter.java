package cn.thens.xflow.flow;

import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import cn.thens.xflow.cancellable.Cancellable;
import cn.thens.xflow.scheduler.Scheduler;
import cn.thens.xflow.scheduler.Schedulers;

/**
 * @author 7hens
 */
public class FlowEmitter<T> implements Emitter<T> {
    private List<Emitter<? super T>> emitters = new CopyOnWriteArrayList<>();
    private CollectorEmitter<T> collectorEmitter = new CollectorEmitter<T>(Schedulers.core()) {
        @Override
        Collector<T> collector() {
            return new Collector<T>() {
                @Override
                public void onCollect(Reply<? extends T> reply) {
                    for (Emitter<? super T> emitter : emitters) {
                        emitter.emit(reply);
                    }
                }
            };
        }
    };

    @Override
    public void emit(Reply<? extends T> reply) {
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
    public void cancel() {
        error(new CancellationException());
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
        final AtomicReference<Emitter<? super T>> emitterRef = new AtomicReference<>(null);
        return Flow.<T>create(emitter -> {
            emitterRef.set(emitter);
            emitters.add(emitter);
        })////////////
                .onCollect(new CollectorHelper<T>() {
                    @Override
                    protected void onTerminate(Throwable e) throws Throwable {
                        super.onTerminate(e);
                        Emitter<? super T> emitter = emitterRef.get();
                        if (emitter != null) {
                            emitters.remove(emitter);
                        }
                    }
                });
    }
}
