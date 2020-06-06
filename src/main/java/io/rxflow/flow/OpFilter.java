package io.rxflow.flow;


import io.rxflow.collector.RxCollector;
import io.rxflow.func.Predicate;

/**
 * @author 7hens
 */
class OpFilter<T> implements RxFlow.Operator<T, T> {
    private final Predicate<? super T> predicate;

    OpFilter(Predicate<? super T> predicate) {
        this.predicate = predicate;
    }

    @Override
    public RxCollector<? super T> apply(RxCollector<? super T> downStream) {
        return new RxCollector<T>() {
            @Override
            public void onCollect(T t) throws Throwable {
                if (predicate.test(t)) {
                    downStream.onCollect(t);
                }
            }

            @Override
            public void onTerminate(Throwable e) throws Throwable {
                downStream.onTerminate(e);
            }
        };
    }
}
