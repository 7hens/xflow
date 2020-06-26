package cn.thens.xflow.flow;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

import cn.thens.xflow.cancellable.Cancellable;
import cn.thens.xflow.scheduler.Scheduler;

class FlowOnBackpressure<T> extends Flow<T> {
    private final Flow<T> upFlow;
    private final Backpressure<T> backpressure;
    private final LinkedList<T> queue = new LinkedList<>();
    private AtomicBoolean isCollecting = new AtomicBoolean(false);

    FlowOnBackpressure(Flow<T> upFlow, Backpressure<T> backpressure) {
        this.upFlow = upFlow;
        this.backpressure = backpressure;
    }

    @Override
    protected Cancellable collect(Scheduler scheduler, Collector<T> collector) {
        CollectorEmitter<T> emitter = newDownEmitter(scheduler, collector);
        emitter.scheduler().schedule(new Runnable() {
            @Override
            public void run() {
                try {
                    onStart(emitter);
                } catch (Throwable e) {
                    emitter.error(e);
                }
            }
        });
        return emitter;
    }

    private void onStart(CollectorEmitter<T> emitter) throws Throwable {
        upFlow.collect(emitter, new Collector<T>() {
            @Override
            public void onCollect(Reply<T> reply) {
                if (emitter.isTerminated()) return;
                if (reply.isTerminated()) {
                    emitter.emit(reply);
                    return;
                }
                if (isCollecting.compareAndSet(false, true)) {
                    emitter.emit(reply);
                } else {
                    queue.add(reply.data());
                    try {
                        backpressure.apply(queue);
                    } catch (Throwable e) {
                        emitter.error(e);
                    }
                }
            }
        });
    }

    private CollectorEmitter<T> newDownEmitter(Scheduler scheduler, Collector<T> collector) {
        return new CollectorEmitter<T>(scheduler) {
            @Override
            Collector<T> collector() {
                return new Collector<T>() {
                    @Override
                    public void onCollect(Reply<T> reply) {
                        collector.onCollect(reply);
                        if (reply.isTerminated()) {
                            queue.clear();
                            return;
                        }
                        if (!queue.isEmpty()) {
                            data(queue.poll());
                        } else {
                            isCollecting.set(false);
                        }
                    }
                };
            }
        };
    }
}
