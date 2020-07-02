package cn.thens.xflow.flow;


import java.util.concurrent.CancellationException;

import cn.thens.xflow.cancellable.Cancellable;

/**
 * @author 7hens
 */
public abstract class CollectorHelper<T> implements Collector<T> {
    @Override
    public void onCollect(Reply<? extends T> reply) {
        try {
            if (!reply.isTerminal()) {
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
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    protected void onStart(Cancellable cancellable) throws Throwable {
    }

    protected void onEach(T data) throws Throwable {
    }

    protected void onTerminate(Throwable error) throws Throwable {
    }

    protected void onComplete() throws Throwable {
    }

    protected void onError(Throwable error) throws Throwable {
    }

    protected void onCancel() throws Throwable {
    }

    public static <T> CollectorHelper<T> from(final Emitter<? super T> emitter) {
        return new CollectorHelper<T>() {
            @Override
            public void onCollect(Reply<? extends T> reply) {
                super.onCollect(reply);
                emitter.emit(reply);
            }
        };
    }

    @SuppressWarnings("unchecked")
    public static <T> CollectorHelper<T> wrap(final Collector<? super T> collector) {
        if (collector instanceof CollectorHelper) {
            return (CollectorHelper) collector;
        }
        return new CollectorHelper<T>() {
            @Override
            public void onCollect(Reply<? extends T> reply) {
                super.onCollect(reply);
                collector.onCollect(reply);
            }
        };
    }

    private static CollectorHelper INSTANCE = new CollectorHelper() {
    };

    @SuppressWarnings("unchecked")
    public static <T> CollectorHelper<T> get() {
        return INSTANCE;
    }
}
