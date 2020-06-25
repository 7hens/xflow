package cn.thens.xflow.flow;


import java.util.concurrent.atomic.AtomicBoolean;

import cn.thens.xflow.func.Func1;
import cn.thens.xflow.func.Funcs;

/**
 * @author 7hens
 */
class FlowThrottleFirst<T> implements Flow.Operator<T, T> {
    private final Func1<T, Flow<?>> flowFactory;
    private final AtomicBoolean isLastFlowTerminated = new AtomicBoolean(true);

    private FlowThrottleFirst(Func1<T, Flow<?>> flowFactory) {
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
                    if (isLastFlowTerminated.compareAndSet(true, false)) {
                        flowFactory.invoke(reply.data()).collect(emitter, new CollectorHelper() {
                            @Override
                            protected void onTerminate(Throwable error) throws Throwable {
                                super.onTerminate(error);
                                isLastFlowTerminated.set(true);
                            }
                        });
                        emitter.emit(reply);
                    }
                } catch (Throwable e) {
                    emitter.error(e);
                }
            }
        };
    }

    static <T> Flow.Operator<T, T> throttleFirst(Func1<T, Flow<?>> flowFactory) {
        return new FlowThrottleFirst<T>(flowFactory);
    }

    static <T> Flow.Operator<T, T> throttleFirst(Flow<?> flow) {
        return new FlowThrottleFirst<T>(Funcs.always(flow));
    }
}
