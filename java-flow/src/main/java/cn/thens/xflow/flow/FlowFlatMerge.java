package cn.thens.xflow.flow;

/**
 * @author 7hens
 */
class FlowFlatMerge<T> implements Flow.Operator<Flow<T>, T> {
    @Override
    public Collector<Flow<T>> apply(Emitter<T> emitter) {
        return new Collector<Flow<T>>() {
            final FlowFlatHelper helper = FlowFlatHelper.create(emitter);

            @Override
            public void onCollect(Reply<Flow<T>> reply) {
                helper.onOuterCollect(reply);
                if (reply.isTerminated()) return;
                Flow<T> flow = reply.data();
                flow.collect(innerCollector, emitter.scheduler());
            }

            private final Collector<T> innerCollector = new Collector<T>() {
                @Override
                public void onCollect(Reply<T> reply) {
                    helper.onInnerCollect(reply);
                    if (reply.isTerminated()) return;
                    emitter.emit(reply);
                }
            };
        };
    }
}
