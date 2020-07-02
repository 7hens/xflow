package cn.thens.xflow.flow;


import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import cn.thens.xflow.func.BiFunction;

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
                if (reply.isTerminal()) {
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

    static <T, R> FlowReduce<T, R>
    reduce(Flow<T> upFlow, R initialValue, BiFunction<? super R, ? super T, ? extends R> accumulator) {
        return new FlowReduce<T, R>(upFlow) {
            @Override
            protected void onStart(CollectorEmitter<? super R> emitter) {
                value.set(initialValue);
                super.onStart(emitter);
            }

            @Override
            void accumulate(T data) throws Throwable {
                value.set(accumulator.apply(value.get(), data));
            }
        };
    }

    static <T> FlowReduce<T, T>
    reduce(Flow<T> upFlow, BiFunction<? super T, ? super T, ? extends T> accumulator) {
        return new FlowReduce<T, T>(upFlow) {
            private AtomicBoolean hasValue = new AtomicBoolean(false);

            @Override
            void accumulate(T data) throws Throwable {
                if (hasValue.compareAndSet(false, true)) {
                    value.set(data);
                } else {
                    value.set(accumulator.apply(value.get(), data));
                }
            }
        };
    }
}
