package io.rxflow.flow;

import io.rxflow.collector.RxCollector;
import io.rxflow.func.Func;

/**
 * @author 7hens
 */
public class OpMap<T, R> implements RxFlow.Operator<T, R> {
    private final Func<? super T, ? extends R> mapper;

    public OpMap(Func<? super T, ? extends R> mapper) {
        this.mapper = mapper;
    }

    @Override
    public RxCollector<? super T> apply(RxCollector<? super R> downStream) {
        return new RxCollector<T>() {
            @Override
            public void onCollect(T t) throws Throwable {
                downStream.onCollect(mapper.apply(t));
            }

            @Override
            public void onTerminate(Throwable e) throws Throwable {
                downStream.onTerminate(e);
            }
        };
    }
}
