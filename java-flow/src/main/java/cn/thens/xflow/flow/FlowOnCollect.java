package cn.thens.xflow.flow;

/**
 * @author 7hens
 */
class FlowOnCollect<T> extends AbstractFlow<T> {
    private final Flow<T> upFlow;
    private final Collector<T> collector;

    FlowOnCollect(Flow<T> upFlow, Collector<T> collector) {
        this.upFlow = upFlow;
        this.collector = collector;
    }

    @Override
    protected void onStart(CollectorEmitter<? super T> emitter) throws Throwable {
        if (collector instanceof CollectorHelper) {
            try {
                ((CollectorHelper<T>) collector).onStart(emitter);
            } catch (Throwable e) {
                emitter.error(e);
            }
        }
        upFlow.collect(emitter, new Collector<T>() {
            @Override
            public void onCollect(Reply<? extends T> reply) {
                try {
                    collector.onCollect(reply);
                } catch (Throwable e) {
                    emitter.error(e);
                }
                emitter.emit(reply);
            }
        });
    }
}
