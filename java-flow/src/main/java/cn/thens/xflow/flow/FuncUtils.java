package cn.thens.xflow.flow;


import cn.thens.xflow.func.Func1;
import cn.thens.xflow.func.Funcs;

/**
 * @author 7hens
 */
@SuppressWarnings("unchecked")
final class FuncUtils {
    private static Func1 SELF = new Func1() {
        @Override
        public Object invoke(Object o) {
            return o;
        }
    };

    static <P1, R> Func1<P1, R> self() {
        return SELF;
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, R>
    Funcs.Result<P1, P2, P3, P4, P5, P6, P7, P8, P9, R> always(R value) {
        return value != null
                ? Funcs.<P1, P2, P3, P4, P5, P6, P7, P8, P9, R>result(value)
                : Funcs.<P1, P2, P3, P4, P5, P6, P7, P8, P9, R>empty();
    }
}
