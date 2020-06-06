package io.rxflow.flow;

import java.util.concurrent.atomic.AtomicBoolean;

import io.rxflow.collector.RxCollector;
import io.rxflow.func.Cancellable;

/**
 * @author 7hens
 */
class OpLift<Up, Dn> implements RxFlow.Converter<Up, RxFlow<Dn>> {
    private final RxFlow.Operator<? super Up, ? extends Dn> operator;

    OpLift(RxFlow.Operator<? super Up, ? extends Dn> operator) {
        this.operator = operator;
    }

    @Override
    public RxFlow<Dn> apply(RxFlow<Up> upStream) {
        return new RxFlow<Dn>() {
            @Override
            public Cancellable collect(RxCollector<? super Dn> collector) {
                return upStream.collect(operator.apply(new RxCollector<Dn>() {
                    private AtomicBoolean isTerminated = new AtomicBoolean(false);

                    @Override
                    public void onCollect(Dn up) throws Throwable {
                        if (isTerminated.get()) return;
                        collector.onCollect(up);
                    }

                    @Override
                    public void onTerminate(Throwable e) throws Throwable {
                        if (isTerminated.compareAndSet(false, true)) {
                            collector.onTerminate(e);
                        }
                    }
                }));
            }
        };
    }
}
