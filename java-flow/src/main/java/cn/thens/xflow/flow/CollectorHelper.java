package cn.thens.xflow.flow;


import java.util.concurrent.CancellationException;

import cn.thens.xflow.cancellable.Cancellable;
import cn.thens.xflow.func.Action0;
import cn.thens.xflow.func.Action1;

/**
 * @author 7hens
 */
public class CollectorHelper<T> implements Collector<T> {
    @Override
    public void onCollect(Reply<T> reply) {
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

    public CollectorHelper<T> onStart(Action1<Cancellable> action) {
        return new CollectorHelper.Wrapper<T>(this) {
            @Override
            protected void onStart(Cancellable cancellable) throws Throwable {
                super.onStart(cancellable);
                action.invoke(cancellable);
            }
        };
    }

    public CollectorHelper<T> onEach(Action1<T> action) {
        return new CollectorHelper.Wrapper<T>(this) {
            @Override
            protected void onEach(T data) throws Throwable {
                super.onEach(data);
                action.invoke(data);
            }
        };
    }

    public CollectorHelper<T> onTerminate(final Action1<Throwable> action) {
        return new CollectorHelper.Wrapper<T>(this) {
            @Override
            protected void onTerminate(Throwable error) throws Throwable {
                super.onTerminate(error);
                action.invoke(error);
            }
        };
    }

    public CollectorHelper<T> onComplete(Action0 action) {
        return new CollectorHelper.Wrapper<T>(this) {
            @Override
            protected void onComplete() throws Throwable {
                super.onComplete();
                action.invoke();
            }
        };
    }

    public CollectorHelper<T> onError(Action1<Throwable> action) {
        return new CollectorHelper.Wrapper<T>(this) {
            @Override
            protected void onError(Throwable error) throws Throwable {
                super.onError(error);
                action.invoke(error);
            }
        };
    }

    public CollectorHelper<T> onCancel(Action0 action) {
        return new CollectorHelper.Wrapper<T>(this) {
            @Override
            protected void onCancel() throws Throwable {
                super.onCancel();
                action.invoke();
            }
        };
    }

    public static <T> CollectorHelper<T> from(final Emitter<T> emitter) {
        return new CollectorHelper<T>() {
            @Override
            public void onCollect(Reply<T> reply) {
                super.onCollect(reply);
                emitter.emit(reply);
            }
        };
    }

    public static <T> CollectorHelper<T> wrap(final Collector<T> collector) {
        if (collector instanceof CollectorHelper) {
            return (CollectorHelper<T>) collector;
        }
        return new CollectorHelper<T>() {
            @Override
            public void onCollect(Reply<T> reply) {
                super.onCollect(reply);
                collector.onCollect(reply);
            }
        };
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
        public void onCollect(Reply<T> reply) {
            super.onCollect(reply);
            collector.onCollect(reply);
        }
    }
}
