package cn.thens.xflow.func;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author 7hens
 */
@SuppressWarnings("WeakerAccess")
public final class Funcs {
    public static <T> T call(@NotNull Func0<? extends T> func) {
        try {
            return func.invoke();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T call(@NotNull Func0<? extends T> func, @Nullable T fallback) {
        try {
            return call(func);
        } catch (Throwable e) {
            return fallback;
        }
    }

    private static Func1 SELF = new Func1() {
        @Override
        public Object invoke(Object o) {
            return o;
        }
    };

    @SuppressWarnings("unchecked")
    public static <P1, R> Func1<P1, R> self() {
        return SELF;
    }

    private static Result EMPTY_RESULT = always(null);

    @SuppressWarnings("unchecked")
    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, R>
    Result<P1, P2, P3, P4, P5, P6, P7, P8, P9, R> empty() {
        return EMPTY_RESULT;
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, R>
    Result<P1, P2, P3, P4, P5, P6, P7, P8, P9, R> always(R value) {
        if (value == null) return empty();
        return new Result<>(value);
    }

    public static final class Result<P1, P2, P3, P4, P5, P6, P7, P8, P9, R> implements
            Func0<R>,
            Func1<P1, R>,
            Func2<P1, P2, R>,
            Func3<P1, P2, P3, R>,
            Func4<P1, P2, P3, P4, R>,
            Func5<P1, P2, P3, P4, P5, R>,
            Func6<P1, P2, P3, P4, P5, P6, R>,
            Func7<P1, P2, P3, P4, P5, P6, P7, R>,
            Func8<P1, P2, P3, P4, P5, P6, P7, P8, R>,
            Func9<P1, P2, P3, P4, P5, P6, P7, P8, P9, R> {

        private final R result;

        private Result(R result) {
            this.result = result;
        }

        @Override
        public R invoke() {
            return result;
        }

        @Override
        public R invoke(P1 p1) {
            return result;
        }

        @Override
        public R invoke(P1 p1, P2 p2) {
            return result;
        }

        @Override
        public R invoke(P1 p1, P2 p2, P3 p3) {
            return result;
        }

        @Override
        public R invoke(P1 p1, P2 p2, P3 p3, P4 p4) {
            return result;
        }

        @Override
        public R invoke(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5) {
            return result;
        }

        @Override
        public R invoke(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6) {
            return result;
        }

        @Override
        public R invoke(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6, P7 p7) {
            return result;
        }

        @Override
        public R invoke(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6, P7 p7, P8 p8) {
            return result;
        }

        @Override
        public R invoke(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6, P7 p7, P8 p8, P9 p9) {
            return result;
        }
    }
}
