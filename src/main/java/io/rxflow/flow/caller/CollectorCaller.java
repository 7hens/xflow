package io.rxflow.flow.caller;

import org.jetbrains.annotations.NotNull;

import io.rxflow.flow.reply.Reply;

/**
 * @author 7hens
 */
public class CollectorCaller<T> implements Caller<T> {
    private final CollectorHelper<T> collector;

    public CollectorCaller(Collector<T> collector) {
        this.collector = new CollectorHelper<>(collector);
    }

    @Override
    public void receive(@NotNull Reply<T> reply) {
        if (reply.over()) {
            collector.onTerminate(reply.error());
            return;
        }
        try {
            collector.onCollect(reply.value());
            if (!collector.isTerminated()) {
                reply.callee().reply(this);
            }
        } catch (Throwable e) {
            collector.onTerminate(e);
        }
    }
}
