package io.rxflow.flow.caller;

import io.rxflow.flow.reply.Reply;

public interface Caller<T> {
    void receive(Reply<T> reply);
}
