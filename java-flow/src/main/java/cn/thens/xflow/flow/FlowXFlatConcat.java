package cn.thens.xflow.flow;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author 7hens
 */
public class FlowXFlatConcat<T> implements Flow.Operator<Flow<T>, T> {
    @Override
    public Collector<Flow<T>> apply(final Emitter<T> emitter) {
        return new Collector<Flow<T>>() {
            final Queue<Flow<T>> flowQueue = new LinkedList<>();
            final AtomicBoolean isCollecting = new AtomicBoolean(false);
            final FlowXFlatHelper helper = FlowXFlatHelper.create(emitter);

            @Override
            public void onCollect(Reply<Flow<T>> reply) {
                helper.onOuterCollect(reply);
                if (reply.isTerminated()) return;
                Flow<T> flow = reply.data();
                if (isCollecting.compareAndSet(false, true)) {
                    flow.collect(innerCollector, emitter.scheduler());
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
                            flowQueue.poll().collect(this, emitter.scheduler());
                        } else {
                            isCollecting.set(false);
                        }
                        return;
                    }
                    emitter.emit(reply);
                }
            };
        };
    }
}
