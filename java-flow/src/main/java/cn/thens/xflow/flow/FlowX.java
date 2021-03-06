package cn.thens.xflow.flow;

import java.util.concurrent.atomic.AtomicReference;

import cn.thens.xflow.cancellable.Cancellable;
import cn.thens.xflow.func.Function;
import cn.thens.xflow.scheduler.Scheduler;

/**
 * @author 7hens
 */
@SuppressWarnings("WeakerAccess")
public final class FlowX {
    private FlowX() {
    }

    public static <T> Function<Flow<? extends Flowable<T>>, PolyFlow<T>> poly() {
        return flow -> new PolyFlow<T>() {
            @Override
            protected Cancellable collect(Scheduler scheduler, Collector<? super Flowable<T>> collector) {
                return flow.collect(scheduler, collector);
            }
        };
    }

    public static <Up, Dn> Operator<Up, Dn> pipe(Function<? super Flow<Up>, ? extends Flowable<Dn>> action) {
        return new Operator<Up, Dn>() {
            @Override
            public Collector<? super Up> apply(Emitter<? super Dn> emitter) throws Throwable {
                AtomicReference<Emitter<? super Up>> upEmitterRef = new AtomicReference<>();
                Flow.<Up>create(upEmitterRef::set).to(action).asFlow().collect(emitter);
                return reply -> upEmitterRef.get().emit(reply);
            }
        };
    }

    public static <Up, Dn> Operator<Up, Dn> wrap(FlowOperator<Up, Dn> operator) {
        return new Operator<Up, Dn>() {
            @Override
            public Collector<? super Up> apply(Emitter<? super Dn> emitter) throws Throwable {
                return operator.apply(emitter);
            }
        };
    }

    public static abstract class Operator<Up, Dn> implements FlowOperator<Up, Dn> {
        public <T> Operator<Up, T> then(FlowOperator<Dn, T> operator) {
            Operator<Up, Dn> self = this;
            return new Operator<Up, T>() {
                @Override
                public Collector<? super Up> apply(Emitter<? super T> emitter) throws Throwable {
                    return self.apply(CollectorEmitter.create(emitter.scheduler(), operator.apply(emitter)));
                }
            };
        }
    }
}
