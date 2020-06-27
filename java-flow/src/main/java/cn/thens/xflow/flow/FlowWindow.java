package cn.thens.xflow.flow;

/**
 * @author 7hens
 */
class FlowWindow<T> extends AbstractPolyFlow<T> {
    private final Flow<T> upFlow;
    private final Flowable<?> windowFlowable;
    private Emitter<? super T> currentEmitter;

    private FlowWindow(Flow<T> upFlow, Flowable<?> windowFlowable) {
        this.upFlow = upFlow;
        this.windowFlowable = windowFlowable;
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
        windowFlowable.asFlow().collect(emitter, new CollectorHelper() {
            @Override
            protected void onTerminate(Throwable error) throws Throwable {
                if (currentEmitter != null) {
                    currentEmitter.complete();
                }
                emitNewFlow(emitter);
            }
        });
    }

    static <T> FlowWindow<T> window(Flow<T> upFlow, Flowable<?> windowFlowable) {
        return new FlowWindow<>(upFlow, windowFlowable);
    }
}
