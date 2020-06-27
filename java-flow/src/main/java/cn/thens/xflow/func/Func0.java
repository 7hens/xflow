package cn.thens.xflow.func;

/**
 * @author 7hens
 */
public interface Func0<R> extends Func<R> {
    R invoke() throws Throwable;
}
