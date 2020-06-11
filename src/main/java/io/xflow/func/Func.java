package io.xflow.func;

/**
 * @author 7hens
 */
public interface Func<T, R> {
    R apply(T t);
}
