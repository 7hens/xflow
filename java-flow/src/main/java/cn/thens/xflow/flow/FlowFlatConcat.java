package cn.thens.xflow.flow;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 7hens
 */
public class FlowFlatConcat<T> implements Flow.Operator<Flow<T>, T> {
    @Override
    public Collector<Flow<T>> apply(final Emitter<T> emitter) {
        return new Collector<Flow<T>>() {
            private final Queue<Flow<T>> flowQueue = new LinkedList<>();
            private final AtomicBoolean isCollecting = new AtomicBoolean(false);
            private final AtomicInteger restFlowCount = new AtomicInteger(1);

            @Override
            public void onCollect(Reply<Flow<T>> reply) {
                if (reply.isTerminated()) {
                    Throwable error = reply.error();
                    if (error == null) {
                        if (restFlowCount.decrementAndGet() == 0) {
                            emitter.complete();
                        }
                    } else {
                        emitter.error(error);
                    }
                    return;
                }
                Flow<T> flow = reply.data();
                restFlowCount.getAndIncrement();
                if (isCollecting.compareAndSet(false, true)) {
                    flow.collect(downCollector, emitter.scheduler());
                    return;
                }
                flowQueue.add(flow);
            }

            private final Collector<T> downCollector = new Collector<T>() {
                @Override
                public void onCollect(Reply<T> reply) {
                    isCollecting.set(true);
                    if (reply.isTerminated()) {
                        if (restFlowCount.decrementAndGet() == 0) {
                            emitter.complete();
                            isCollecting.set(false);
                            return;
                        }
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
