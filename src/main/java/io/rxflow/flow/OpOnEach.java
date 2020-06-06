package io.rxflow.flow;

import io.rxflow.collector.RxCollector;
import io.rxflow.func.Consumer;

/**
 * @author 7hens
 */
class OpOnEach<T> implements RxFlow.Operator<T, T> {
    private final Consumer<? super T> consumer;

    OpOnEach(Consumer<? super T> consumer) {
        this.consumer = consumer;
    }

    @Override
    public RxCollector<? super T> apply(RxCollector<? super T> downStream) {
        return new RxCollector<T>() {
            @Override
            public void onCollect(T t) throws Throwable {
                consumer.accept(t);
                downStream.onCollect(t);
            }

            @Override
            public void onTerminate(Throwable e) throws Throwable {
                downStream.onTerminate(e);
            }
        };
    }
}
