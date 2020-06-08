package io.rxflow.flow.reply;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.rxflow.flow.callee.Callee;
import io.rxflow.func.Func;

public abstract class Reply<T> {
    public abstract boolean over();

    @Nullable
    public abstract Throwable error();

    @Nullable
    public abstract T value();

    @NotNull
    public abstract Callee<T> callee();

    public static <T> Reply<T> of(final T value, final Callee<T> callee) {
        return new ReplyValue<>(value, callee);
    }

    public static <T> Reply<T> of(final Throwable e) {
        return new ReplyError<>(e);
    }
}
