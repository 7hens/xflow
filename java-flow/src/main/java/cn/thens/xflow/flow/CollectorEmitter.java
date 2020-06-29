package cn.thens.xflow.flow;

import java.util.LinkedList;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;

import cn.thens.xflow.cancellable.CompositeCancellable;
import cn.thens.xflow.scheduler.CancellableScheduler;
import cn.thens.xflow.scheduler.Scheduler;

/**
 * @author 7hens
 */
class CollectorEmitter<T> extends CompositeCancellable implements Emitter<T>, Collector<T> {
    private final AtomicBoolean isCollecting = new AtomicBoolean(false);
    private final AtomicBoolean isTerminated = new AtomicBoolean(false);
    private final LinkedList<T> buffer = new LinkedList<>();
    private Reply<? extends T> terminalReply = null;
    private final CancellableScheduler scheduler;
    private final Collector<? super T> collector;
    private final Backpressure<T> backpressure;

    private CollectorEmitter(Scheduler scheduler, Collector<? super T> collector, Backpressure<T> backpressure) {
        this.scheduler = scheduler.cancellable();
        this.collector = wrapCollector(collector);
        this.backpressure = backpressure;
    }

    @Override
    public void emit(Reply<? extends T> reply) {
        onCollect(reply);
    }

    @Override
    public void onCollect(Reply<? extends T> reply) {
        if (isTerminated.get()) {
            return;
        }
        if (reply.isTerminated()) {
            isTerminated.set(true);
        }
        if (reply.isCancel()) {
            collector.onCollect(reply);
            super.cancel();
            return;
        }
        if (isCollecting.compareAndSet(false, true)) {
            scheduler.schedule(() -> collector.onCollect(reply));
            return;
        }
        if (reply.isTerminated()) {
            terminalReply = reply;
            return;
        }
        try {
            buffer.add(reply.data());
            backpressure.apply(buffer);
        } catch (Throwable e) {
            error(e);
        }
    }

    private Collector<T> wrapCollector(Collector<? super T> collector) {
        return new Collector<T>() {
            @Override
            public void onCollect(Reply<? extends T> reply) {
                isCollecting.set(true);
                collector.onCollect(reply);
                if (reply.isTerminated()) {
                    buffer.clear();
                    CollectorEmitter.super.cancel();
                    return;
                }
                if (!buffer.isEmpty()) {
                    onCollect(Reply.data(buffer.poll()));
                } else if (terminalReply != null) {
                    onCollect(terminalReply);
                } else {
                    isCollecting.set(false);
                }
            }
        };
    }

    @Override
    public void data(T data) {
        emit(Reply.data(data));
    }

    @Override
    public void error(Throwable error) {
        emit(Reply.error(error));
    }

    @Override
    public void cancel() {
        error(new CancellationException());
    }

    @Override
    public void complete() {
        emit(Reply.complete());
    }

    @Override
    public boolean isTerminated() {
        return isTerminated.get();
    }

    @Override
    public Scheduler scheduler() {
        return scheduler;
    }

    @Override
    protected void onCancel() {
        super.onCancel();
        scheduler.cancel();
    }

    static <T> CollectorEmitter<T> create(Scheduler scheduler, Collector<? super T> collector, Backpressure<T> backpressure) {
        return new CollectorEmitter<T>(scheduler, collector, backpressure);
    }

    private static final Backpressure DEFAULT_BACKPRESSURE = Backpressure.buffer(16);

    @SuppressWarnings("unchecked")
    static <T> CollectorEmitter<T> create(Scheduler scheduler, Collector<? super T> collector) {
        return create(scheduler, collector, DEFAULT_BACKPRESSURE);
    }
}
