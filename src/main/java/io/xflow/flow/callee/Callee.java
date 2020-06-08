package io.xflow.flow.callee;

import org.jetbrains.annotations.NotNull;

import io.xflow.flow.caller.Caller;

public interface Callee<T> {
    void reply(@NotNull Caller<T> caller);
}
