package io.rxflow.flow.reply;

import org.jetbrains.annotations.NotNull;

import io.rxflow.flow.callee.Callee;

public abstract class ReplyWrapper<T> extends Reply<T> {
    protected abstract Reply<T> baseReply();

    @Override
    public boolean over() {
        return baseReply().over();
    }

    @Override
    public Throwable error() {
        return baseReply().error();
    }

    @Override
    public T value() {
        return baseReply().value();
    }

    @NotNull
    @Override
    public Callee<T> callee() {
        return baseReply().callee();
    }
}
