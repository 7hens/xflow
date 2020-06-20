package cn.thens.xflow.flow;

import java.util.concurrent.CancellationException;

import cn.thens.xflow.cancellable.CompositeCancellable;

/**
 * @author 7hens
 */
class FlowOnCollect<T> implements Flow.Operator<T, T> {
    private final Collector<T> collector;

    FlowOnCollect(Collector<T> collector) {
        this.collector = collector;
    }

    @Override
    public Collector<T> apply(Emitter<T> emitter) {
        if (collector instanceof CollectorHelper) {
            ((CollectorHelper<T>) collector).onStart(new CompositeCancellable() {
                @Override
                protected void onCancel() {
                    super.onCancel();
                    emitter.error(new CancellationException());
                }
            });
        }
        return new Collector<T>() {
            @Override
            public void onCollect(Reply<T> reply) {
                collector.onCollect(reply);
                emitter.emit(reply);
            }
        };
    }
}
