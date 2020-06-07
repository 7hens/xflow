package io.rxflow.flow.reply;

import io.rxflow.flow.callee.Callee;
import io.rxflow.func.Func;

class ReplyMap<T, R> extends Reply<R> {
    private final Reply<T> reply;
    private final Func<T, R> mapper;

    ReplyMap(Reply<T> reply, Func<T, R> mapper) {
        this.reply = reply;
        this.mapper = mapper;
    }

    @Override
    public boolean over() {
        return reply.over();
    }

    @Override
    public Throwable error() {
        return reply.error();
    }

    @Override
    public R value() {
        try {
            return mapper.apply(reply.value());
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    @Override
    public Callee<R> callee() {
        return caller -> {
            reply.callee().reply(reply -> {
                caller.receive(this.reply.map(mapper));
            });
        };
    }
}
