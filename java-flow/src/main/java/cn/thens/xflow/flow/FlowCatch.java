package cn.thens.xflow.flow;


import cn.thens.xflow.func.Func1;
import cn.thens.xflow.func.Funcs;
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
    protected void onStart(CollectorEmitter<T> emitter) {
        upFlow.collect(emitter, new Collector<T>() {
            @Override
            public void onCollect(Reply<T> reply) {
                if (reply.isTerminated()) {
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

    abstract void handleError(Throwable error, Emitter<T> emitter) throws Throwable;

    static <T> Flow<T> catchError(Flow<T> upFlow, Func1<Throwable, Flow<T>> resumeFunc) {
        return new FlowCatch<T>(upFlow) {
            @Override
            void handleError(Throwable error, Emitter<T> emitter) throws Throwable {
                resumeFunc.invoke(error).collect(emitter);
            }
        };
    }

    static <T> Flow<T> catchError(Flow<T> upFlow, Flow<T> resumeFlow) {
        return catchError(upFlow, Funcs.result(resumeFlow));
    }

    static <T> Flow<T> retry(Flow<T> upFlow, Predicate<Throwable> predicate) {
        return new FlowCatch<T>(upFlow) {
            @Override
            void handleError(Throwable error, Emitter<T> emitter) throws Throwable {
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
        return retry(upFlow, PredicateHelper.always());
    }

    static <T> Flow<T> retry(Flow<T> upFlow, int count) {
        return retry(upFlow, PredicateHelper.take(count));
    }
}
