package io.rxflow.flow.caller;

import io.rxflow.flow.reply.Reply;

public abstract class ContinueCaller<T> implements Caller<T> {
    protected abstract Caller<T> baseCaller();

    @Override
    public void receive(Reply<T> reply) {
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

    public static <T> ContinueCaller<T> create(Collector<T> collector) {
        return of(reply -> {
            if (reply.over()) {
                collector.onTerminate(reply.error());
            } else {
                collector.onCollect(reply.value());
            }
        });
    }
}
