package cn.thens.xflow.flow;

import java.util.concurrent.atomic.AtomicReference;

import cn.thens.xflow.cancellable.Cancellable;

/**
 * @author 7hens
 */
class FlowXFlatSwitch<T> implements Flow.Operator<Flow<T>, T> {
    @Override
    public Collector<Flow<T>> apply(Emitter<T> emitter) {
        return new CollectorHelper<Flow<T>>() {
            final AtomicReference<Cancellable> lastCancellable = new AtomicReference<>(null);
            final FlowXFlatHelper helper = FlowXFlatHelper.create(emitter);

            @Override
            public void onCollect(Reply<Flow<T>> reply) {
                helper.onOuterCollect(reply);
                if (reply.isTerminated()) return;
                Cancellable cancellable = lastCancellable.get();
                if (cancellable != null) {
                    cancellable.cancel();
                }
                Flow<T> flow = reply.data();
                lastCancellable.set(flow.collect(innerCollector, emitter.scheduler()));
            }

            private final Collector<T> innerCollector = new Collector<T>() {
                @Override
                public void onCollect(Reply<T> reply) {
                    if (reply.isCancel()) {
                        helper.onInnerCollect(Reply.complete());
                        return;
                    }
                    helper.onInnerCollect(reply);
                    if (reply.isTerminated()) return;
                    emitter.emit(reply);
                }
            };
        };
    }
}
