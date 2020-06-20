package cn.thens.xflow.flow;

/**
 * @author 7hens
 */
public abstract class Reply<T> {
    public abstract boolean isTerminated();

    public abstract Throwable error();

    public abstract T data();

    public boolean isComplete() {
        return isTerminated() && error() == null;
    }

    public boolean isError() {
        return isTerminated() && error() != null;
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
