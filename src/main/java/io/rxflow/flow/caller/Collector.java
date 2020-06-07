package io.rxflow.flow.caller;

public interface Collector<T> {
    void onCollect(T t);

    void onTerminate(Throwable e);
}
