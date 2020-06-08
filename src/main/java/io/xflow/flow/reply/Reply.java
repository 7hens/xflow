package io.xflow.flow.reply;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.xflow.flow.callee.Callee;

public interface Reply<T> {
    boolean over();

    @Nullable
    Throwable error();

    @Nullable
    T value();

    @NotNull
    Callee<T> callee();
}
