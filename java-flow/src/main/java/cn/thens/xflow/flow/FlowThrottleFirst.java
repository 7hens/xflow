package cn.thens.xflow.flow;


import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;

import cn.thens.xflow.cancellable.Cancellable;
import cn.thens.xflow.cancellable.CompositeCancellable;
import cn.thens.xflow.func.Function;
import cn.thens.xflow.func.Functions;

/**
 * @author 7hens
 */
class FlowThrottleFirst<T> implements FlowOperator<T, T> {
    private final Function<? super T, ? extends Flowable<?>> flowFactory;
    private final AtomicBoolean couldEmit = new AtomicBoolean(true);
    private Cancellable lastFlow = CompositeCancellable.cancelled();

    private FlowThrottleFirst(Function<? super T, ? extends Flowable<?>> flowFactory) {
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
                    lastFlow = flowFactory.apply(reply.data()).asFlow()
                            .collect(emitter, new CollectorHelper() {
                                @Override
                                protected void onTerminate(Throwable error) throws Throwable {
                                    super.onTerminate(error);
                                    if (!(error instanceof CancellationException)) {
                                        couldEmit.set(true);
                                    }
                                }
                            });
                    if (couldEmit.compareAndSet(true, false)) {
                        emitter.emit(reply);
                    }
                } catch (Throwable e) {
                    emitter.error(e);
                }
            }
        };
    }

    static <T> FlowOperator<T, T> throttleFirst(Function<? super T, ? extends Flowable<?>> flowFactory) {
        return new FlowThrottleFirst<>(flowFactory);
    }

    static <T> FlowOperator<T, T> throttleFirst(Flowable<?> flow) {
        return new FlowThrottleFirst<>(Functions.always(flow));
    }
}
