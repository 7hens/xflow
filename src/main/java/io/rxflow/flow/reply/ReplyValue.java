package io.rxflow.flow.reply;

import java.util.NoSuchElementException;

import io.rxflow.flow.callee.Callee;

class ReplyValue<T> extends Reply<T> {
    private final T value;
    private final Callee<T> callee;

    ReplyValue(T value, Callee<T> callee) {
        this.value = value;
        this.callee = callee;
    }

    @Override
    public boolean over() {
        return false;
    }

    @Override
    public Throwable error() {
        throw new NoSuchElementException("This is not a final replay");
    }

    @Override
    public T value() {
        return value;
    }

    @Override
    public Callee<T> callee() {
        return callee;
    }
}
