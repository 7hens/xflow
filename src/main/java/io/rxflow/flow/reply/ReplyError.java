package io.rxflow.flow.reply;

import org.jetbrains.annotations.NotNull;

import java.util.NoSuchElementException;

import io.rxflow.flow.callee.Callee;

class ReplyError<T> extends Reply<T> {
    private final Throwable e;

    ReplyError(Throwable e) {
        this.e = e;
    }

    @Override
    public boolean over() {
        return true;
    }

    @Override
    public Throwable error() {
        return e;
    }

    @Override
    public T value() {
        throw new NoSuchElementException("This is a final replay");
    }

    @NotNull
    @Override
    public Callee<T> callee() {
        throw new NoSuchElementException("This is a final replay");
    }
}
