package cn.thens.xflow.func;

/**
 * @author 7hens
 */
public interface Predicate<T> {
    boolean test(T t) throws Throwable;
}
