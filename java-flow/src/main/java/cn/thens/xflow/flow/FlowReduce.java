package cn.thens.xflow.flow;


import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import cn.thens.xflow.func.Func2;

/**
 * @author 7hens
 */
abstract class FlowReduce<T, R> extends AbstractFlow<R> {
    private final Flow<T> upFlow;
    final AtomicReference<R> value = new AtomicReference<>(null);

    private FlowReduce(Flow<T> upFlow) {
        this.upFlow = upFlow;
    }

    @Override
    protected void onStart(CollectorEmitter<? super R> emitter) {
        upFlow.collect(emitter, new Collector<T>() {
            private AtomicBoolean hasValue = new AtomicBoolean(false);

            @Override
            public void onCollect(Reply<? extends T> reply) {
                if (reply.isTerminated()) {
                    Throwable error = reply.error();
                    if (error == null) {
                        if (hasValue.get()) {
                            emitter.data(value.get());
                        }
                        emitter.complete();
                    } else {
                        emitter.error(error);
                    }
                    return;
                }
                try {
                    accumulate(reply.data());
                    hasValue.set(true);
                } catch (Throwable e) {
                    emitter.error(e);
                }
            }
        });
    }

    abstract void accumulate(T data) throws Throwable;

    static <T, R> FlowReduce<T, R> reduce(Flow<T> upFlow, R initialValue, Func2<R, ? super T, R> accumulator) {
        return new FlowReduce<T, R>(upFlow) {
            @Override
            void accumulate(T data) throws Throwable {
                value.set(accumulator.invoke(value.get(), data));
            }
        };
    }

    static <T> FlowReduce<T, T> reduce(Flow<T> upFlow, Func2<T, ? super T, T> accumulator) {
        return new FlowReduce<T, T>(upFlow) {
            private AtomicBoolean hasValue = new AtomicBoolean(false);

            @Override
            void accumulate(T data) throws Throwable {
                if (hasValue.compareAndSet(false, true)) {
                    value.set(data);
                } else {
                    value.set(accumulator.invoke(value.get(), data));
                }
            }
        };
    }
}
