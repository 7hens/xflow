package io.rxflow.func;

/**
 * @author 7hens
 */
public interface Cancellable {
    void cancel() throws Throwable;
}
