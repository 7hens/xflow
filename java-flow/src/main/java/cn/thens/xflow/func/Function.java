package cn.thens.xflow.func;

/**
 * @author 7hens
 */
public interface Function<P, R> {
    R apply(P p) throws Throwable;
}
