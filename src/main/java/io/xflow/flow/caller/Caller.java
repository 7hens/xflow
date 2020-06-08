package io.xflow.flow.caller;

import org.jetbrains.annotations.NotNull;

import io.xflow.flow.reply.Reply;

public interface Caller<T> {
    void receive(@NotNull Reply<T> reply);
}
