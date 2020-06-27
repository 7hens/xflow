package cn.thens.xflow.flow;

import cn.thens.xflow.func.Func0;
import cn.thens.xflow.func.Funcs;

/**
 * @author 7hens
 */
class FlowWindow<T> extends AbstractPolyFlow<T > {
    private final Flow<T> upFlow;
    private final Func0<? extends Flow<?>> windowFlowFactory;
    private Emitter<? super T> currentEmitter;

    private FlowWindow(Flow<T> upFlow, Func0<? extends Flow<?>> windowFlowFactory) {
        this.upFlow = upFlow;
        this.windowFlowFactory = windowFlowFactory;
    }

    @Override
    protected void onStart(CollectorEmitter<? super Flowable<T>> emitter) throws Throwable {
        emitNewFlow(emitter);
        upFlow.collect(emitter, reply -> {
            emitter.scheduler().schedule(new Runnable() {
                @Override
                public void run() {
                    if (currentEmitter != null) {
                        currentEmitter.emit(reply);
                    }
                }
            });
        });
    }

    @SuppressWarnings("unchecked")
    private void emitNewFlow(CollectorEmitter<? super Flow<T>> emitter) throws Throwable {
        emitter.data(new AbstractFlow<T>() {
            @Override
            protected void onStart(CollectorEmitter<? super T> innerEmitter) throws Throwable {
                currentEmitter = innerEmitter;
            }
        });
        windowFlowFactory.invoke().collect(emitter, new CollectorHelper() {
            @Override
            protected void onTerminate(Throwable error) throws Throwable {
                if (currentEmitter != null) {
                    currentEmitter.complete();
                }
                emitNewFlow(emitter);
            }
        });
    }

    static <T> FlowWindow<T> window(Flow<T> upFlow, Func0<Flow<?>> windowFlowFactory) {
        return new FlowWindow<>(upFlow, windowFlowFactory);
    }

    static <T> FlowWindow<T> window(Flow<T> upFlow, Flow<?> windowFlow) {
        return new FlowWindow<>(upFlow, Funcs.always(windowFlow));
    }
}
