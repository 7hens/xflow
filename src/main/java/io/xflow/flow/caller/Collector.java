package io.xflow.flow.caller;

import org.jetbrains.annotations.Nullable;

import io.xflow.func.Action;

/**
 * @author 7hens
 */
public interface Collector<T> {
    void onEach(T t, Action action);

    void onTerminate(@Nullable Throwable e, Action action);
}
