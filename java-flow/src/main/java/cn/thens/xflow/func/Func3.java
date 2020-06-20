package cn.thens.xflow.func;

/**
 * @author 7hens
 */
public interface Func3<P1, P2, P3, R> extends Func<R> {
    R invoke(P1 p1, P2 p2, P3 p3) throws Throwable;
}
