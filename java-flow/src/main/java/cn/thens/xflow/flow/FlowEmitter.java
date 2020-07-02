package cn.thens.xflow.flow;

import java.util.ArrayList;
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
@SuppressWarnings("WeakerAccess")
public abstract class FlowEmitter<T> implements Emitter<T>, Flowable<T> {
    private final List<Emitter<? super T>> emitters = new CopyOnWriteArrayList<>();
    private Reply<? extends T> terminalReply = null;
    private final CollectorEmitter<T> collectorEmitter =
            CollectorEmitter.create(Schedulers.core(), new Collector<T>() {
                @Override
                public void onCollect(Reply<? extends T> reply) {
                    collector().onCollect(reply);
                    for (Emitter<? super T> emitter : emitters) {
                        emitter.emit(reply);
                    }
                    if (reply.isTerminal()) {
                        terminalReply = reply;
                        emitters.clear();
                    }
                }
            });

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

    protected abstract Collector<T> collector();

    @Override
    public Flow<T> asFlow() {
        if (isTerminated()) return Flow.error(terminalReply.error());
        final AtomicReference<Emitter<? super T>> emitterRef = new AtomicReference<>(null);
        return Flow.<T>create(emitter -> {
            emitterRef.set(emitter);
            emitters.add(emitter);
        }).onTerminate(it -> {
            Emitter<? super T> emitter = emitterRef.get();
            if (emitter != null) {
                emitters.remove(emitter);
            }
        });
    }

    public static <T> FlowEmitter<T> publish() {
        return new FlowEmitter<T>() {
            @Override
            protected Collector<T> collector() {
                return CollectorHelper.get();
            }
        };
    }

    private static <T> FlowEmitter<T> behavior(Reply<? extends T> initReply) {
        return new FlowEmitter<T>() {
            private Reply<? extends T> lastReply = initReply;

            @Override
            protected Collector<T> collector() {
                return reply -> {
                    if (!reply.isTerminal()) {
                        lastReply = reply;
                    }
                };
            }

            @Override
            public Flow<T> asFlow() {
                if (lastReply != null) {
                    return super.asFlow().polyWith(Flow.just(lastReply.data())).flatMerge();
                }
                return super.asFlow();
            }
        };
    }

    public static <T> FlowEmitter<T> behavior() {
        return behavior(null);
    }

    public static <T> FlowEmitter<T> behavior(T initData) {
        return behavior(Reply.data(initData));
    }

    public static <T> FlowEmitter<T> replay() {
        return new FlowEmitter<T>() {
            private List<Reply<? extends T>> replyBuffer = new ArrayList<>();

            @Override
            protected Collector<T> collector() {
                return reply -> replyBuffer.add(reply);
            }

            @Override
            public Flow<T> asFlow() {
                return Flow.<T>create(emitter -> {
                    for (Reply<? extends T> reply : replyBuffer) {
                        emitter.emit(reply);
                    }
                    emitter.complete();
                }).polyWith(super.asFlow()).flatConcat();
            }
        };
    }
}
