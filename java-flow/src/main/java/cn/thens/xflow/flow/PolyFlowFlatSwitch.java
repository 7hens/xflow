package cn.thens.xflow.flow;

import java.util.concurrent.atomic.AtomicReference;

import cn.thens.xflow.cancellable.Cancellable;

/**
 * @author 7hens
 */
class PolyFlowFlatSwitch<T> extends AbstractFlow<T> {
    private final PolyFlow<T> upFlow;

    PolyFlowFlatSwitch(PolyFlow<T> upFlow) {
        this.upFlow = upFlow;
    }

    @Override
    protected void onStart(CollectorEmitter<? super T> emitter) throws Throwable {
        upFlow.collect(emitter, new Collector<Flowable<T>>() {
            final AtomicReference<Cancellable> lastCancellable = new AtomicReference<>(null);
            final PolyFlowFlatHelper helper = PolyFlowFlatHelper.create(emitter);

            @Override
            public void onCollect(Reply<? extends Flowable<T>> reply) {
                helper.onOuterCollect(reply);
                if (reply.isTerminated()) return;
                Cancellable cancellable = lastCancellable.get();
                if (cancellable != null) {
                    cancellable.cancel();
                }
                Flowable<T> flow = reply.data();
                try {
                    lastCancellable.set(flow.asFlow().collect(emitter, innerCollector));
                } catch (Throwable e) {
                    emitter.error(e);
                }
            }

            private final Collector<T> innerCollector = new Collector<T>() {
                @Override
                public void onCollect(Reply<? extends T> reply) {
                    if (reply.isCancel()) {
                        helper.onInnerCollect(Reply.complete());
                        return;
                    }
                    helper.onInnerCollect(reply);
                    if (reply.isTerminated()) return;
                    emitter.emit(reply);
                }
            };
        });
    }
}
