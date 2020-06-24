package cn.thens.xflow.flow;

/**
 * @author 7hens
 */
class FlowAutoCancel<T> extends AbstractFlow<T> {
    private final Flow<T> upFlow;
    private final Flow<?> cancelFlow;

    FlowAutoCancel(Flow<T> upFlow, Flow<?> cancelFlow) {
        this.upFlow = upFlow;
        this.cancelFlow = cancelFlow;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onStart(CollectorEmitter<T> emitter) throws Throwable {
        upFlow.collect(emitter);
        cancelFlow.collect(emitter, new CollectorHelper() {
            @Override
            protected void onTerminate(Throwable error) throws Throwable {
                emitter.cancel();
            }
        });
    }
}
