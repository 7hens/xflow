package cn.thens.xflow.flow;

/**
 * @author 7hens
 */
class PolyFlowFlatMerge<T> extends AbstractFlow<T> {
    private final PolyFlow<T> upFlow;

    PolyFlowFlatMerge(PolyFlow<T> upFlow) {
        this.upFlow = upFlow;
    }

    @Override
    protected void onStart(CollectorEmitter<? super T> emitter) throws Throwable {
        upFlow.collect(emitter, new Collector<Flowable<T>>() {
            final PolyFlowFlatHelper helper = PolyFlowFlatHelper.create(emitter);

            @Override
            public void onCollect(Reply<? extends Flowable<T>> reply) {
                helper.onOuterCollect(reply);
                if (reply.isTerminated()) return;
                Flowable<T> flowable = reply.data();
                flowable.asFlow().collect(emitter, innerCollector);
            }

            private final Collector<T> innerCollector = new Collector<T>() {
                @Override
                public void onCollect(Reply<? extends T> reply) {
                    helper.onInnerCollect(reply);
                    if (reply.isTerminated()) return;
                    emitter.emit(reply);
                }
            };
        });
    }
}
