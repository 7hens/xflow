package cn.thens.xflow.flow;

import java.util.List;

import cn.thens.xflow.func.Func0;
import cn.thens.xflow.func.Funcs;

/**
 * @author 7hens
 */
class FlowWindow<T> extends AbstractFlow<Flow<T>> {
    private final Flow<T> upFlow;
    private final Func0<Flow<?>> windowFlowFactory;
    private Emitter<T> currentEmitter;

    private FlowWindow(Flow<T> upFlow, Func0<Flow<?>> windowFlowFactory) {
        this.upFlow = upFlow;
        this.windowFlowFactory = windowFlowFactory;
    }

    @Override
    protected void onStart(CollectorEmitter<Flow<T>> emitter) throws Throwable {
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
    private void emitNewFlow(CollectorEmitter<Flow<T>> emitter) throws Throwable {
        emitter.data(new AbstractFlow<T>() {
            @Override
            protected void onStart(CollectorEmitter<T> innerEmitter) throws Throwable {
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
        return new FlowWindow<>(upFlow, Funcs.result(windowFlow));
    }

    static <T> Flow<List<T>> buffer(Flow<T> upFlow, Func0<Flow<?>> windowFlowFactory) {
        return window(upFlow, windowFlowFactory).flatMap(Flow::toList);
    }

    static <T> Flow<List<T>> buffer(Flow<T> upFlow, Flow<?> windowFlow) {
        return buffer(upFlow, Funcs.result(windowFlow));
    }
}
