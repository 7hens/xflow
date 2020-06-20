package cn.thens.xflow.func;

/**
 * @author 7hens
 */
public interface Func1<P1, R> extends Func<R> {
    R invoke(P1 p1) throws Throwable;
}
