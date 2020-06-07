package io.rxflow.flow.reply;

import io.rxflow.flow.callee.Callee;
import io.rxflow.func.Func;

public abstract class Reply<T> {
    public abstract boolean over();

    public abstract Throwable error();

    public abstract T value();

    public abstract Callee<T> callee();

    public <R> Reply<R> map(Func<T, R> mapper) {
        return new ReplyMap<>(this, mapper);
    }

    public static <T> Reply<T> of(final T value, final Callee<T> callee) {
        return new ReplyValue<>(value, callee);
    }

    public static <T> Reply<T> of(final Throwable e) {
        return new ReplyError<>(e);
    }
}
