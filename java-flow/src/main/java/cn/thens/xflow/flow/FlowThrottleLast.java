package cn.thens.xflow.flow;


import java.util.concurrent.CancellationException;

import cn.thens.xflow.cancellable.Cancellable;
import cn.thens.xflow.cancellable.CompositeCancellable;
import cn.thens.xflow.func.Func1;
import cn.thens.xflow.func.Funcs;

/**
 * @author 7hens
 */
class FlowThrottleLast<T> implements Flow.Operator<T, T> {
    private final Func1<T, Flow<?>> flowFactory;
    private Cancellable lastFlow = CompositeCancellable.cancelled();

    private FlowThrottleLast(Func1<T, Flow<?>> flowFactory) {
        this.flowFactory = flowFactory;
    }

    @Override
    public Collector<T> apply(Emitter<T> emitter) {
        return new Collector<T>() {
            @SuppressWarnings("unchecked")
            @Override
            public void onCollect(Reply<T> reply) {
                if (reply.isTerminated()) {
                    emitter.emit(reply);
                    return;
                }
                try {
                    lastFlow.cancel();
                    lastFlow = flowFactory.invoke(reply.data()).collect(emitter, new CollectorHelper() {
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

    static <T> Flow.Operator<T, T> throttleLast(Func1<T, Flow<?>> flowFactory) {
        return new FlowThrottleLast<T>(flowFactory);
    }

    static <T> Flow.Operator<T, T> throttleLast(Flow<?> flow) {
        return new FlowThrottleLast<T>(Funcs.always(flow));
    }
}
