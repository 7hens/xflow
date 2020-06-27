package cn.thens.xflow.flow;


import java.util.concurrent.CancellationException;

import cn.thens.xflow.cancellable.Cancellable;
import cn.thens.xflow.func.Action0;
import cn.thens.xflow.func.Action1;

/**
 * @author 7hens
 */
public abstract class CollectorHelper<T> implements Collector<T> {
    @Override
    public void onCollect(Reply<? extends T> reply) {
        try {
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

    public final CollectorHelper<T> onStart(Action1<? super Cancellable> action) {
        return new CollectorHelper.Wrapper<T>(this) {
            @Override
            protected void onStart(Cancellable cancellable) throws Throwable {
                super.onStart(cancellable);
                action.invoke(cancellable);
            }
        };
    }

    public final CollectorHelper<T> onEach(Action1<? super T> action) {
        return new CollectorHelper.Wrapper<T>(this) {
            @Override
            protected void onEach(T data) throws Throwable {
                super.onEach(data);
                action.invoke(data);
            }
        };
    }

    public final CollectorHelper<T> onTerminate(final Action1<? super Throwable> action) {
        return new CollectorHelper.Wrapper<T>(this) {
            @Override
            protected void onTerminate(Throwable error) throws Throwable {
                super.onTerminate(error);
                action.invoke(error);
            }
        };
    }

    public final CollectorHelper<T> onComplete(Action0 action) {
        return new CollectorHelper.Wrapper<T>(this) {
            @Override
            protected void onComplete() throws Throwable {
                super.onComplete();
                action.invoke();
            }
        };
    }

    public final CollectorHelper<T> onError(Action1<? super Throwable> action) {
        return new CollectorHelper.Wrapper<T>(this) {
            @Override
            protected void onError(Throwable error) throws Throwable {
                super.onError(error);
                action.invoke(error);
            }
        };
    }

    public final CollectorHelper<T> onCancel(Action0 action) {
        return new CollectorHelper.Wrapper<T>(this) {
            @Override
            protected void onCancel() throws Throwable {
                super.onCancel();
                action.invoke();
            }
        };
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

    public static class Wrapper<T> extends CollectorHelper<T> {
        private final CollectorHelper<T> collector;

        public Wrapper(Collector<T> collector) {
            this.collector = wrap(collector);
        }

        @Override
        protected void onStart(Cancellable cancellable) throws Throwable {
            super.onStart(cancellable);
            collector.onStart(cancellable);
        }

        @Override
        public void onCollect(Reply<? extends T> reply) {
            super.onCollect(reply);
            collector.onCollect(reply);
        }
    }
}
