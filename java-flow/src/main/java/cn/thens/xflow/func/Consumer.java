package cn.thens.xflow.func;

/**
 * @author 7hens
 */
public interface Consumer<T> {
    void accept(T t) throws Throwable;
}
