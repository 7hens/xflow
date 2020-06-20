package cn.thens.xflow.flow;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 7hens
 */
class FlowFlatMerge<T> implements Flow.Operator<Flow<T>, T> {
    @Override
    public Collector<Flow<T>> apply(Emitter<T> emitter) {
        return new CollectorHelper<Flow<T>>() {
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
                flow.collect(downCollector, emitter.scheduler());
            }

            private final Collector<T> downCollector = new Collector<T>() {
                @Override
                public void onCollect(Reply<T> reply) {
                    if (reply.isTerminated()) {
                        if (restFlowCount.decrementAndGet() == 0) {
                            emitter.complete();
                        }
                        return;
                    }
                    emitter.emit(reply);
                }
            };
        };
    }
}
