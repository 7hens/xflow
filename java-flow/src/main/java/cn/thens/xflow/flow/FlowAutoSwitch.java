package cn.thens.xflow.flow;

import java.util.concurrent.CancellationException;

import cn.thens.xflow.cancellable.Cancellable;

/**
 * @author 7hens
 */
class FlowAutoSwitch<T> extends AbstractFlow<T> {
    private final Flow<T> upFlow;
    private final Flowable<?> switchFlow;
    private final Flowable<T> fallback;

    private FlowAutoSwitch(Flow<T> upFlow, Flowable<?> switchFlow, Flowable<T> fallback) {
        this.upFlow = upFlow;
        this.switchFlow = switchFlow;
        this.fallback = fallback;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onStart(CollectorEmitter<? super T> emitter) throws Throwable {
        Cancellable cancellable = upFlow.collect(emitter, new Collector<T>() {
            @Override
            public void onCollect(Reply<? extends T> reply) {
                if (reply.isCancel()) return;
                emitter.emit(reply);
            }
        });
        switchFlow.asFlow().collect(emitter, new CollectorHelper() {
            @Override
            protected void onTerminate(Throwable error) throws Throwable {
                cancellable.cancel();
                fallback.asFlow().collect(emitter);
            }
        });
    }

    static <T> FlowAutoSwitch<T> autoSwitch(Flow<T> upFlow, Flowable<?> switchFlow, Flowable<T> fallback) {
        return new FlowAutoSwitch<>(upFlow, switchFlow, fallback);
    }

    static <T> FlowAutoSwitch<T> autoCancel(Flow<T> upFlow, Flowable<?> switchFlow) {
        return autoSwitch(upFlow, switchFlow, Flow.error(new CancellationException()));
    }
}
