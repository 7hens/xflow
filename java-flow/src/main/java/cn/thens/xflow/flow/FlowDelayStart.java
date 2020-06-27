package cn.thens.xflow.flow;


import cn.thens.xflow.func.Func0;
import cn.thens.xflow.func.Funcs;

/**
 * @author 7hens
 */
class FlowDelayStart<T> extends AbstractFlow<T> {
    private final Flow<T> upFlow;
    private final Func0<? extends Flow> delayFunc;

    private FlowDelayStart(Flow<T> upFlow, Func0<? extends Flow<?>> delayFunc) {
        this.upFlow = upFlow;
        this.delayFunc = delayFunc;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onStart(CollectorEmitter<? super T> emitter) {
        try {
            delayFunc.invoke().collect(emitter, new CollectorHelper() {
                @Override
                protected void onTerminate(Throwable error) throws Throwable {
                    super.onTerminate(error);
                    upFlow.collect(emitter);
                }
            });
        } catch (Throwable e) {
            emitter.error(e);
        }
    }

    public static <T> FlowDelayStart<T> delayStart(Flow<T> upFlow, Func0<? extends Flow<?>> delayFunc) {
        return new FlowDelayStart<>(upFlow, delayFunc);
    }

    public static <T> FlowDelayStart<T> delayStart(Flow<T> upFlow, Flow<?> delayFlow) {
        return delayStart(upFlow, Funcs.always(delayFlow));
    }
}
