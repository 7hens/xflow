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
    protected void onStart(CollectorEmitter<T> emitter) throws Throwable {
        upFlow.collect(emitter, new Collector<Flow<T>>() {
            final Queue<Flow<T>> flowQueue = new LinkedList<>();
            final AtomicBoolean isCollecting = new AtomicBoolean(false);
            final PolyFlowFlatHelper helper = PolyFlowFlatHelper.create(emitter);

            @Override
            public void onCollect(Reply<Flow<T>> reply) {
                helper.onOuterCollect(reply);
                if (reply.isTerminated()) return;
                Flow<T> flow = reply.data();
                if (isCollecting.compareAndSet(false, true)) {
                    flow.collect(emitter, innerCollector);
                    return;
                }
                flowQueue.add(flow);
            }

            private final Collector<T> innerCollector = new Collector<T>() {
                @Override
                public void onCollect(Reply<T> reply) {
                    helper.onInnerCollect(reply);
                    if (emitter.isTerminated()) {
                        flowQueue.clear();
                        return;
                    }
                    isCollecting.set(true);
                    if (reply.isTerminated()) {
                        if (!flowQueue.isEmpty()) {
                            flowQueue.poll().collect(emitter, this);
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
