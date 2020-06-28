package cn.thens.xflow.func;

/**
 * @author 7hens
 */
public interface BiFunction<P1, P2, R> {
    R apply(P1 p1, P2 p2) throws Throwable;
}
