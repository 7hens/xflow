package cn.thens.xflow.flow;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;

import cn.thens.xflow.cancellable.CompositeCancellable;
import cn.thens.xflow.scheduler.CancellableScheduler;
import cn.thens.xflow.scheduler.Scheduler;

/**
 * @author 7hens
 */
abstract class CollectorEmitter<T> extends CompositeCancellable implements Emitter<T>, Collector<T>, Runnable {
    private final AtomicBoolean isCollecting = new AtomicBoolean(false);
    private final AtomicBoolean isTerminated = new AtomicBoolean(false);
    private final Queue<Reply<? extends T>> replyQueue = new LinkedList<>();
    private final CancellableScheduler scheduler;

    CollectorEmitter(Scheduler scheduler) {
        this.scheduler = scheduler.cancellable();
    }

    abstract Collector<T> collector();

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
            collector().onCollect(reply);
            super.cancel();
            return;
        }
        replyQueue.add(reply);
        if (isCollecting.compareAndSet(false, true)) {
            scheduler.schedule(this);
        }
    }

    @Override
    public void run() {
        isCollecting.set(true);
        try {
            while (!isCancelled() && !replyQueue.isEmpty()) {
                Reply<? extends T> reply = replyQueue.poll();
                collector().onCollect(reply);
                if (reply.isTerminated()) {
                    super.cancel();
                    return;
                }
            }
        } finally {
            isCollecting.set(false);
        }
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

    static <T> CollectorEmitter<T> create(Scheduler scheduler, Collector<T> collector) {
        return new CollectorEmitter<T>(scheduler) {
            @Override
            Collector<T> collector() {
                return collector;
            }
        };
    }
}
