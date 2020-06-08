package io.rxflow.flow.callee;

import org.jetbrains.annotations.NotNull;

import io.rxflow.flow.caller.Caller;

public interface Callee<T> {
    void reply(@NotNull Caller<T> caller);
}
