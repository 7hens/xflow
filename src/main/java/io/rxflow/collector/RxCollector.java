package io.rxflow.collector;

/**
 * @author 7hens
 */
public interface RxCollector<T> {
    void onCollect(T t) throws Throwable;

    void onTerminate(Throwable e) throws Throwable;
}
