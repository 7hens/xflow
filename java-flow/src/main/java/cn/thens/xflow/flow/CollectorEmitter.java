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
class CollectorEmitter<T> extends CompositeCancellable implements Emitter<T>, Collector<T>, Runnable {
    private final AtomicBoolean isCollecting = new AtomicBoolean(false);
    private final AtomicBoolean isTerminated = new AtomicBoolean(false);
    private final Queue<Reply<T>> replyQueue = new LinkedList<>();
    private final Collector<T> collector;
    private final CancellableScheduler scheduler;

    CollectorEmitter(Collector<T> collector, Scheduler scheduler) {
        this.scheduler = scheduler.cancellable();
        this.collector = collector;
    }

    @Override
    public void emit(Reply<T> reply) {
        onCollect(reply);
    }

    @Override
    public void onCollect(Reply<T> reply) {
        if (isTerminated.get()) {
            return;
        }
        if (reply.isTerminated()) {
            isTerminated.set(true);
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
            while (!replyQueue.isEmpty()) {
                Reply<T> reply = replyQueue.poll();
                collector.onCollect(reply);
                if (reply.isTerminated()) {
                    scheduler.cancel();
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
        error(new CancellationException());
    }
}
