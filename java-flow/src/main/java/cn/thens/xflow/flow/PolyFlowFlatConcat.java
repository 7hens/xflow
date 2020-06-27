package cn.thens.xflow.flow;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author 7hens
 */
class PolyFlowFlatConcat<T> extends AbstractFlow<T> {
    private final PolyFlow<T> upFlow;

    PolyFlowFlatConcat(PolyFlow<T> upFlow) {
        this.upFlow = upFlow;
    }

    @Override
    protected void onStart(CollectorEmitter<? super T> emitter) throws Throwable {
        upFlow.collect(emitter, new Collector<Flowable<T>>() {
            final Queue<Flowable<T>> flowQueue = new LinkedList<>();
            final AtomicBoolean isCollecting = new AtomicBoolean(false);
            final PolyFlowFlatHelper helper = PolyFlowFlatHelper.create(emitter);

            @Override
            public void onCollect(Reply<? extends Flowable<T>> reply) {
                helper.onOuterCollect(reply);
                if (reply.isTerminated()) return;
                Flowable<T> flowable = reply.data();
                if (isCollecting.compareAndSet(false, true)) {
                    try {
                        flowable.asFlow().collect(emitter, innerCollector);
                    } catch (Throwable e) {
                        emitter.error(e);
                    }
                    return;
                }
                flowQueue.add(flowable);
            }

            private final Collector<T> innerCollector = new Collector<T>() {
                @Override
                public void onCollect(Reply<? extends T> reply) {
                    helper.onInnerCollect(reply);
                    if (emitter.isTerminated()) {
                        flowQueue.clear();
                        return;
                    }
                    isCollecting.set(true);
                    if (reply.isTerminated()) {
                        if (!flowQueue.isEmpty()) {
                            try {
                                flowQueue.poll().asFlow().collect(emitter, this);
                            } catch (Throwable e) {
                                emitter.error(e);
                            }
                        } else {
                            isCollecting.set(false);
                        }
                        return;
                    }
                    emitter.emit(reply);
                }
            };
        });
    }
}
