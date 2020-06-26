package cn.thens.xflow.flow;


import java.util.concurrent.atomic.AtomicBoolean;

import cn.thens.xflow.cancellable.Cancellable;
import cn.thens.xflow.cancellable.CompositeCancellable;
import cn.thens.xflow.func.Func1;
import cn.thens.xflow.func.Funcs;

/**
 * @author 7hens
 */
class FlowThrottleFirst<T> implements FlowOperator<T, T> {
    private final Func1<T, Flow<?>> flowFactory;
    private final AtomicBoolean couldEmit = new AtomicBoolean(true);
    private Cancellable lastFlow = CompositeCancellable.cancelled();

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
                    lastFlow.cancel();
                    lastFlow = flowFactory.invoke(reply.data()).collect(emitter, new CollectorHelper() {
                        @Override
                        protected void onTerminate(Throwable error) throws Throwable {
                            super.onTerminate(error);
                            couldEmit.set(true);
                        }
                    });
                    System.out.println("##, " + reply.data() + ", couldEmit: " + couldEmit.get());
                    if (couldEmit.compareAndSet(true, false)) {
                        emitter.emit(reply);
                    }
                } catch (Throwable e) {
                    emitter.error(e);
                }
            }
        };
    }

    static <T> FlowOperator<T, T> throttleFirst(Func1<T, Flow<?>> flowFactory) {
        return new FlowThrottleFirst<>(flowFactory);
    }

    static <T> FlowOperator<T, T> throttleFirst(Flow<?> flow) {
        return new FlowThrottleFirst<>(Funcs.always(flow));
    }
}
