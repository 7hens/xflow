package cn.thens.xflow.flow;


import java.util.concurrent.CancellationException;

import cn.thens.xflow.cancellable.Cancellable;
import cn.thens.xflow.cancellable.CompositeCancellable;
import cn.thens.xflow.func.Func1;
import cn.thens.xflow.func.Funcs;

/**
 * @author 7hens
 */
class FlowThrottleLast<T> implements FlowOperator<T, T> {
    private final Func1<? super T, ? extends Flowable<?>> flowFactory;
    private Cancellable lastFlow = CompositeCancellable.cancelled();

    private FlowThrottleLast(Func1<? super T, ? extends Flowable<?>> flowFactory) {
        this.flowFactory = flowFactory;
    }

    @Override
    public Collector<T> apply(Emitter<? super T> emitter) {
        return new Collector<T>() {
            @SuppressWarnings("unchecked")
            @Override
            public void onCollect(Reply<? extends T> reply) {
                if (reply.isTerminated()) {
                    emitter.emit(reply);
                    return;
                }
                try {
                    lastFlow.cancel();
                    lastFlow = flowFactory.invoke(reply.data())
                            .asFlow()
                            .collect(emitter, new CollectorHelper() {
                                @Override
                                protected void onTerminate(Throwable error) throws Throwable {
                                    super.onTerminate(error);
                                    if (!(error instanceof CancellationException)) {
                                        emitter.emit(reply);
                                    }
                                }
                            });
                } catch (Throwable e) {
                    emitter.error(e);
                }
            }
        };
    }

    static <T> FlowOperator<T, T> throttleLast(Func1<? super T, ? extends Flowable<?>> flowFactory) {
        return new FlowThrottleLast<T>(flowFactory);
    }

    static <T> FlowOperator<T, T> throttleLast(Flowable<?> flow) {
        return new FlowThrottleLast<T>(Funcs.always(flow));
    }
}
