package cn.thens.xflow.flow;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author 7hens
 */
class FlowFlatZip<T> implements Flow.Operator<Flow<T>, List<T>> {
    private final boolean delayError;

    FlowFlatZip(boolean delayError) {
        this.delayError = delayError;
    }

    @Override
    public Collector<Flow<T>> apply(Emitter<List<T>> emitter) {
        return new CollectorHelper<Flow<T>>() {
            final Queue<Queue<T>> cachedDataQueue = new LinkedList<>();
            final AtomicBoolean isOuterFlowTerminated = new AtomicBoolean(false);
            final FlowFlatHelper helper = FlowFlatHelper.create(delayError, emitter);

            @Override
            public void onCollect(Reply<Flow<T>> reply) {
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
                Flow<T> flow = reply.data();
                Queue<T> dataQueue = new LinkedList<>();
                cachedDataQueue.add(dataQueue);
                flow.collect(newInnerCollector(dataQueue), emitter.scheduler());
            }

            private Collector<T> newInnerCollector(Queue<T> dataQueue) {
                return new Collector<T>() {
                    @Override
                    public void onCollect(Reply<T> reply) {
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
        };
    }
}
