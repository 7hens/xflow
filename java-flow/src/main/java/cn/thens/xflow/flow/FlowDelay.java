package cn.thens.xflow.flow;


import cn.thens.xflow.func.Func1;
import cn.thens.xflow.func.Funcs;

/**
 * @author 7hens
 */
class FlowDelay<T> extends AbstractFlow<T> {
    private final Flow<T> upFlow;
    private final Func1<? super Reply<T>, ? extends Flow> delayFunc;

    private FlowDelay(Flow<T> upFlow, Func1<? super Reply<T>, ? extends Flow<?>> delayFunc) {
        this.upFlow = upFlow;
        this.delayFunc = delayFunc;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onStart(CollectorEmitter<T> emitter) {
        try {
            upFlow.collect(emitter, new Collector<T>() {
                @Override
                public void onCollect(Reply<T> reply) {
                    try {
                        delayFunc.invoke(reply).collect(emitter, new CollectorHelper() {
                            @Override
                            protected void onTerminate(Throwable error) throws Throwable {
                                super.onTerminate(error);
                                emitter.emit(reply);
                            }
                        });
                    } catch (Throwable e) {
                        emitter.error(e);
                    }

                }
            });
        } catch (Throwable e) {
            emitter.error(e);
        }
    }

    public static <T> FlowDelay<T> delay(Flow<T> upFlow, Func1<? super Reply<T>, ? extends Flow<?>> delayFunc) {
        return new FlowDelay<>(upFlow, delayFunc);
    }

    public static <T> FlowDelay<T> delay(Flow<T> upFlow, Flow<?> delayFlow) {
        return new FlowDelay<>(upFlow, Funcs.always(delayFlow));
    }
}
