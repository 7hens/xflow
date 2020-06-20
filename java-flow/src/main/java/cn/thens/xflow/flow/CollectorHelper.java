package cn.thens.xflow.flow;


import java.util.concurrent.CancellationException;

import cn.thens.xflow.cancellable.Cancellable;

/**
 * @author 7hens
 */
public class CollectorHelper<T> implements Collector<T> {
    protected void onStart(Cancellable cancellable) {
    }

    protected void onEach(T data) {
    }

    protected void onTerminate(Throwable error) {
    }

    protected void onComplete() {
    }

    protected void onError(Throwable error) {
    }

    protected void onCancel() {
    }

    @Override
    public void onCollect(Reply<T> reply) {
        if (!reply.isTerminated()) {
            onEach(reply.data());
            return;
        }
        Throwable error = reply.error();
        onTerminate(error);
        if (error != null) {
            onError(error);
            if (error instanceof CancellationException) {
                onCancel();
            }
        } else {
            onComplete();
        }
    }
}
