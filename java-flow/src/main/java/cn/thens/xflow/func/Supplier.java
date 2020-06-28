package cn.thens.xflow.func;

/**
 * @author 7hens
 */
public interface Supplier<R> {
    R get() throws Throwable;
}
