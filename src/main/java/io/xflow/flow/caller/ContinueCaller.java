package io.xflow.flow.caller;

import org.jetbrains.annotations.NotNull;

import io.xflow.flow.reply.Reply;

public abstract class ContinueCaller<T> implements Caller<T> {
    protected abstract Caller<T> baseCaller();

    @Override
    public void receive(@NotNull Reply<T> reply) {
        baseCaller().receive(reply);
        if (!reply.over()) {
            reply.callee().reply(this);
        }
    }

    public static <T> ContinueCaller<T> of(Caller<T> caller) {
        return new ContinueCaller<T>() {
            @Override
            protected Caller<T> baseCaller() {
                return caller;
            }
        };
    }
}
