package io.rxflow.flow;

import io.rxflow.collector.RxCollector;
import io.rxflow.func.Predicate;

/**
 * @author 7hens
 */
class OpAny<T> implements RxFlow.Operator<T, Boolean> {
    private final Predicate<? super T> predicate;

    OpAny(Predicate<? super T> predicate) {
        this.predicate = predicate;
    }

    @Override
    public RxCollector<? super T> apply(RxCollector<? super Boolean> downStream) {
        return new RxCollector<T>() {
            @Override
            public void onCollect(T t) throws Throwable {
                if (predicate.test(t)) {
                    downStream.onCollect(true);
                    downStream.onTerminate(null);
                }
            }

            @Override
            public void onTerminate(Throwable e) throws Throwable {
                downStream.onCollect(false);
                downStream.onTerminate(null);
            }
        };
    }
}
