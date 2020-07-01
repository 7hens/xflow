package cn.thens.xflow.func;

/**
 * @author 7hens
 */
@SuppressWarnings("WeakerAccess")
public final class Functions {
    private static Function SELF = p -> p;

    @SuppressWarnings("unchecked")
    public static <T> Function<T, T> self() {
        return SELF;
    }

    private static Result EMPTY_RESULT = always(null);

    @SuppressWarnings("unchecked")
    public static <P1, P2, R> Result<P1, P2, R> empty() {
        return EMPTY_RESULT;
    }

    public static <P1, P2, R> Result<P1, P2, R> always(R value) {
        if (value == null) return empty();
        return new Result<>(value);
    }

    public static final class Result<P1, P2, R> implements
            Action, Consumer<P1>, BiConsumer<P1, P2>,
            Supplier<R>, Function<P1, R>, BiFunction<P1, P2, R> {

        private final R result;

        private Result(R result) {
            this.result = result;
        }

        @Override
        public R get() {
            return result;
        }

        @Override
        public R apply(P1 p) {
            return result;
        }

        @Override
        public R apply(P1 p1, P2 p2) {
            return result;
        }

        @Override
        public void run() {
        }

        @Override
        public void accept(P1 p1) {
        }

        @Override
        public void accept(P1 p1, P2 p2) throws Throwable {
        }
    }
}
