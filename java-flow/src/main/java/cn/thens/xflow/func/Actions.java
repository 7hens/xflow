package cn.thens.xflow.func;


import org.jetbrains.annotations.NotNull;

/**
 * @author 7hens
 */
public final class Actions {
    private static final Empty EMPTY = new Empty();

    public static void call(@NotNull Action0 action) {
        try {
            action.invoke();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9> Empty<P1, P2, P3, P4, P5, P6, P7, P8, P9> empty() {
        return EMPTY;
    }

    public static final class Empty<P1, P2, P3, P4, P5, P6, P7, P8, P9> implements
            Action0,
            Action1<P1>,
            Action2<P1, P2>,
            Action3<P1, P2, P3>,
            Action4<P1, P2, P3, P4>,
            Action5<P1, P2, P3, P4, P5>,
            Action6<P1, P2, P3, P4, P5, P6>,
            Action7<P1, P2, P3, P4, P5, P6, P7>,
            Action8<P1, P2, P3, P4, P5, P6, P7, P8>,
            Action9<P1, P2, P3, P4, P5, P6, P7, P8, P9> {

        private Empty() {
        }

        @Override
        public void invoke() {

        }

        @Override
        public void invoke(P1 p1) {

        }

        @Override
        public void invoke(P1 p1, P2 p2) {

        }

        @Override
        public void invoke(P1 p1, P2 p2, P3 p3) {
        }

        @Override
        public void invoke(P1 p1, P2 p2, P3 p3, P4 p4) {

        }

        @Override
        public void invoke(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5) {

        }

        @Override
        public void invoke(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6) {

        }

        @Override
        public void invoke(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6, P7 p7) {

        }

        @Override
        public void invoke(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6, P7 p7, P8 p8) {

        }

        @Override
        public void invoke(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6, P7 p7, P8 p8, P9 p9) {

        }
    }
}
