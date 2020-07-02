package cn.thens.xflow.flow;


import cn.thens.xflow.func.BiConsumer;
import cn.thens.xflow.func.Function;
import cn.thens.xflow.func.Predicate;
import cn.thens.xflow.func.PredicateHelper;

/**
 * @author 7hens
 */
abstract class FlowCatch<T> extends AbstractFlow<T> {
    private final Flow<T> upFlow;

    private FlowCatch(Flow<T> upFlow) {
        this.upFlow = upFlow;
    }

    @Override
    protected void onStart(CollectorEmitter<? super T> emitter) {
        upFlow.collect(emitter, new Collector<T>() {
            @Override
            public void onCollect(Reply<? extends T> reply) {
                if (reply.isTerminal()) {
                    Throwable error = reply.error();
                    if (error != null) {
                        try {
                            handleError(error, emitter);
                        } catch (Throwable e) {
                            emitter.error(e);
                        }
                        return;
                    }
                }
                emitter.emit(reply);
            }
        });
    }

    abstract void handleError(Throwable error, Emitter<? super T> emitter) throws Throwable;

    static <T> Flow<T> catchError(Flow<T> upFlow, Function<? super Throwable, ? extends Flowable<T>> resumeFunc) {
        return new FlowCatch<T>(upFlow) {
            @Override
            void handleError(Throwable error, Emitter<? super T> emitter) throws Throwable {
                resumeFunc.apply(error).asFlow().collect(emitter);
            }
        };
    }

    static <T> Flow<T> catchError(Flow<T> upFlow, BiConsumer<? super Throwable, ? super Emitter<? super T>> resumeConsumer) {
        return new FlowCatch<T>(upFlow) {
            @Override
            void handleError(Throwable error, Emitter<? super T> emitter) throws Throwable {
                resumeConsumer.accept(error, emitter);
            }
        };
    }

    static <T> Flow<T> catchError(Flow<T> upFlow, Flowable<T> resumeFlow) {
        return new FlowCatch<T>(upFlow) {
            @Override
            void handleError(Throwable error, Emitter<? super T> emitter) throws Throwable {
                resumeFlow.asFlow().collect(emitter);
            }
        };
    }

    static <T> Flow<T> retry(Flow<T> upFlow, Predicate<? super Throwable> predicate) {
        return new FlowCatch<T>(upFlow) {
            @Override
            void handleError(Throwable error, Emitter<? super T> emitter) throws Throwable {
                boolean shouldRetry = predicate.test(error);
                if (shouldRetry) {
                    collect(emitter);
                } else {
                    emitter.error(error);
                }
            }
        };
    }

    static <T> Flow<T> retry(Flow<T> upFlow) {
        return retry(upFlow, PredicateHelper.alwaysTrue());
    }

    static <T> Flow<T> retry(Flow<T> upFlow, int count) {
        return retry(upFlow, PredicateHelper.take(count));
    }
}
