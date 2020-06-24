package cn.thens.xflow.flow;

import cn.thens.xflow.func.Func0;
import cn.thens.xflow.func.Funcs;

/**
 * @author 7hens
 */
class FlowBuffer<T> extends AbstractFlow<Flow<T>> {
    private final Flow<T> upFlow;
    private final Func0<Flow<?>> windowFlowFactory;
    private Emitter<T> currentEmitter;

    private FlowBuffer(Flow<T> upFlow, Func0<Flow<?>> windowFlowFactory) {
        this.upFlow = upFlow;
        this.windowFlowFactory = windowFlowFactory;
    }

    @Override
    protected void onStart(CollectorEmitter<Flow<T>> emitter) throws Throwable {
        emitNewFlow(emitter);
        upFlow.collect(emitter, reply -> {
            if (currentEmitter != null) {
                currentEmitter.emit(reply);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void emitNewFlow(CollectorEmitter<Flow<T>> emitter) throws Throwable {
        if (currentEmitter != null) {
            currentEmitter.complete();
        }
        emitter.data(new AbstractFlow<T>() {
            @Override
            protected void onStart(CollectorEmitter<T> innerEmitter) throws Throwable {
                currentEmitter = innerEmitter;
            }
        });
        windowFlowFactory.invoke().collect(emitter, new CollectorHelper() {
            @Override
            protected void onTerminate(Throwable error) throws Throwable {
                emitNewFlow(emitter);
            }
        });
    }

    static <T> FlowBuffer<T> buffer(Flow<T> upFlow, Func0<Flow<?>> windowFlowFactory) {
        return new FlowBuffer<>(upFlow, windowFlowFactory);
    }

    static <T> FlowBuffer<T> buffer(Flow<T> upFlow, Flow<?> windowFlow) {
        return new FlowBuffer<>(upFlow, Funcs.result(windowFlow));
    }
}
