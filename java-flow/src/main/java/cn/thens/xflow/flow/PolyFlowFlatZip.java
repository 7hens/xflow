package cn.thens.xflow.flow;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author 7hens
 */
class PolyFlowFlatZip<T> extends AbstractFlow<List<T>> {
    private final PolyFlow<T> upFlow;

    PolyFlowFlatZip(PolyFlow<T> upFlow) {
        this.upFlow = upFlow;
    }

    @Override
    protected void onStart(CollectorEmitter<? super List<T>> emitter) throws Throwable {
        upFlow.collect(emitter, new Collector<Flowable<T>>() {
            final Queue<Queue<T>> cachedDataQueue = new LinkedList<>();
            final AtomicBoolean isOuterFlowTerminated = new AtomicBoolean(false);
            final PolyFlowFlatHelper helper = PolyFlowFlatHelper.create(emitter);

            @Override
            public void onCollect(Reply<? extends Flowable<T>> reply) {
                if (reply.isComplete()) {
                    isOuterFlowTerminated.set(true);
                    tryZip();
                }
                helper.onOuterCollect(reply);
                if (emitter.isTerminated()) {
                    cachedDataQueue.clear();
                    return;
                }
                if (reply.isTerminated()) return;
                Flowable<T> flow = reply.data();
                Queue<T> dataQueue = new LinkedList<>();
                cachedDataQueue.add(dataQueue);
                try {
                    flow.asFlow().collect(emitter, newInnerCollector(dataQueue));
                } catch (Throwable e) {
                    emitter.error(e);
                }
            }

            private Collector<T> newInnerCollector(Queue<T> dataQueue) {
                return new Collector<T>() {
                    @Override
                    public void onCollect(Reply<? extends T> reply) {
                        helper.onInnerCollect(reply);
                        if (emitter.isTerminated()) {
                            cachedDataQueue.clear();
                            return;
                        }
                        if (reply.isTerminated()) return;
                        dataQueue.add(reply.data());
                        tryZip();
                    }
                };
            }

            private void tryZip() {
                if (!isOuterFlowTerminated.get()) return;
                if (cachedDataQueue.isEmpty()) return;
                List<T> result = new ArrayList<>();
                while (true) {
                    for (Queue<T> queue : cachedDataQueue) {
                        if (queue.isEmpty()) return;
                    }
                    for (Queue<T> queue : cachedDataQueue) {
                        result.add(queue.poll());
                    }
                    emitter.data(result);
                }
            }
        });
    }
}
