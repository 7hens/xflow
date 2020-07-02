package cn.thens.xflow.flow;

import java.util.concurrent.CancellationException;

/**
 * @author 7hens
 */
public abstract class Reply<T> {
    public abstract boolean isTerminal();

    public abstract Throwable error();

    public abstract T data();

    public final boolean isComplete() {
        return isTerminal() && error() == null;
    }

    public final boolean isError() {
        return isTerminal() && error() != null;
    }

    public final boolean isCancel() {
        return isTerminal() && error() instanceof CancellationException;
    }

    public final <R> Reply<R> newReply(final R data) {
        Reply<T> self = this;
        return new Reply<R>() {
            @Override
            public boolean isTerminal() {
                return self.isTerminal();
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

    public static <T> Reply<T> data(final T data) {
        return new Reply<T>() {
            @Override
            public boolean isTerminal() {
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
            public boolean isTerminal() {
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
