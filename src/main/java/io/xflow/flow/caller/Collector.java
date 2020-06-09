package io.xflow.flow.caller;

import org.jetbrains.annotations.Nullable;

/**
 * @author 7hens
 */
public interface Collector<T> {
    void onCollect(T t);

    void onTerminate(@Nullable Throwable e);
}
