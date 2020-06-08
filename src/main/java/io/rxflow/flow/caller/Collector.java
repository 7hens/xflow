package io.rxflow.flow.caller;

import org.jetbrains.annotations.Nullable;

public interface Collector<T> {
    void onCollect(@Nullable T t);

    void onTerminate(@Nullable Throwable e);
}
