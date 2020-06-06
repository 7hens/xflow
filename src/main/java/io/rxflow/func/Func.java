package io.rxflow.func;

/**
 * @author 7hens
 */
public interface Func<T, R> {
    R apply(T t) throws Throwable;
}
