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
                if (reply.isTerminal()) return;
                try {
                    reply.data().asFlow().collect(emitter, innerCollector);
                } catch (Throwable e) {
                    emitter.error(e);
                }
            }

            private final Collector<T> innerCollector = new Collector<T>() {
                @Override
                public void onCollect(Reply<? extends T> reply) {
                    helper.onInnerCollect(reply);
                    if (reply.isTerminal()) return;
                    emitter.emit(reply);
                }
            };
        });
    }
}
