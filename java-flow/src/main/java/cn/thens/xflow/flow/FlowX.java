package cn.thens.xflow.flow;

import java.util.concurrent.atomic.AtomicReference;

import cn.thens.xflow.func.Func1;

/**
 * @author 7hens
 */
@SuppressWarnings("unchecked")
public abstract class FlowX<Up, Dn> implements Flow.Operator<Up, Dn> {

    public final <T> FlowX<Up, T> then(Flow.Operator<Dn, T> operator) {
        FlowX<Up, Dn> self = this;
        return new FlowX<Up, T>() {
            @Override
            public Collector<Up> apply(Emitter<T> emitter) {
                return self.apply(new CollectorEmitter(operator.apply(emitter), emitter.scheduler()));
            }
        };
    }

    public static <Up, Dn> FlowX<Up, Dn> wrap(final Flow.Operator<Up, Dn> operator) {
        return new FlowX<Up, Dn>() {
            @Override
            public Collector<Up> apply(Emitter<Dn> emitter) {
                return operator.apply(emitter);
            }
        };
    }

    public static <Up, Dn> FlowX<Up, Dn> pipe(Func1<Flow<Up>, Flow<Dn>> action) {
        return new FlowX<Up, Dn>() {
            @Override
            public Collector<Up> apply(Emitter<Dn> emitter) {
                AtomicReference<Emitter<Up>> upEmitterRef = new AtomicReference<>();
                Flow.create(upEmitterRef::set)
                        .to(action)
                        .collect(emitter, CollectorHelper.from(emitter));
                return reply -> upEmitterRef.get().emit(reply);
            }
        };
    }

    private static FlowX FLAT_DELAY_ERRORS = wrap(new FlowXDelayErrors());

    public static <T> FlowX<Flow<T>, Flow<T>> delayErrors() {
        return FLAT_DELAY_ERRORS;
    }

    private static FlowX FLAT_MERGE = wrap(new FlowXFlatMerge());

    public static <T> FlowX<Flow<T>, T> flatMerge() {
        return FLAT_MERGE;
    }

    private static FlowX FLAT_CONCAT = wrap(new FlowXFlatConcat());

    public static <T> FlowX<Flow<T>, T> flatConcat() {
        return FLAT_CONCAT;
    }

    private static FlowX FLAT_SWITCH = wrap(new FlowXFlatSwitch());

    public static <T> FlowX<Flow<T>, T> flatSwitch() {
        return FLAT_SWITCH;
    }

    private static FlowX FLAT_ZIP = wrap(new FlowXFlatZip());

    public static <T> FlowX<Flow<T>, T> flatZip() {
        return FLAT_ZIP;
    }
}
