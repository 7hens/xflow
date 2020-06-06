package io.rxflow.func;

/**
 * @author 7hens
 */
public interface Predicate<T> {
    boolean test(T t)throws Exception;
}
