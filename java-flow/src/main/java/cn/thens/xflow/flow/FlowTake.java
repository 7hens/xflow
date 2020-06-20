package cn.thens.xflow.flow;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 7hens
 */
class FlowTake<T> implements Flow.Operator<T, T> {
    private final AtomicInteger count;

    FlowTake(int count) {
        this.count = new AtomicInteger(count);
    }

    @Override
    public Collector<T> apply(Emitter<T> emitter) {
        return new Collector<T>() {
            @Override
            public void onCollect(Reply<T> reply) {
                if (reply.isTerminated()) {
                    emitter.error(reply.error());
                    return;
                }
                emitter.emit(reply);
                if (count.decrementAndGet() == 0) {
                    emitter.complete();
                }
            }
        };
    }
}
