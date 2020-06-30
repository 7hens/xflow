package cn.thens.xflow.flow;

import java.util.concurrent.CancellationException;

/**
 * @author 7hens
 */
public abstract class Reply<T> {
    public abstract boolean isTerminated();

    public abstract Throwable error();

    public abstract T data();

    public final boolean isComplete() {
        return isTerminated() && error() == null;
    }

    public final boolean isError() {
        return isTerminated() && error() != null;
    }

    public final boolean isCancel() {
        return isTerminated() && error() instanceof CancellationException;
    }

    public final <R> Reply<R> newReply(final R data) {
        Reply<T> self = this;
        return new Reply<R>() {
            @Override
            public boolean isTerminated() {
                return self.isTerminated();
            }

            @Override
            public Throwable error() {
                return self.error();
            }

            @Override
            public R data() {
                return data;
            }
        };
    }

    public final <R> Reply<R> nullReply() {
        return newReply(null);
    }

    public static <T> Reply<T> data(final T data) {
        return new Reply<T>() {
            @Override
            public boolean isTerminated() {
                return false;
            }

            @Override
            public Throwable error() {
                return null;
            }

            @Override
            public T data() {
                return data;
            }
        };
    }

    public static <T> Reply<T> error(final Throwable error) {
        return new Reply<T>() {
            @Override
            public boolean isTerminated() {
                return true;
            }

            @Override
            public Throwable error() {
                return error;
            }

            @Override
            public T data() {
                return null;
            }
        };
    }

    private static final Reply COMPLETE = error(null);

    @SuppressWarnings("unchecked")
    public static <T> Reply<T> complete() {
        return COMPLETE;
    }
}
