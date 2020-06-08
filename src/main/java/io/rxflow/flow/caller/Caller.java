package io.rxflow.flow.caller;

import org.jetbrains.annotations.NotNull;

import io.rxflow.flow.reply.Reply;

public interface Caller<T> {
    void receive(@NotNull Reply<T> reply);
}
