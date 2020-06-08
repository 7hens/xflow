package io.rxflow.flow.caller;

import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author 7hens
 */
public class CollectorHelper<T> implements Collector<T> {
    private final AtomicBoolean isTerminated = new AtomicBoolean(false);
    private final Collector<T> collector;

    public CollectorHelper(Collector<T> collector) {
        this.collector = collector;
    }

    @Override
    public void onCollect(@Nullable T t) {
        if (isTerminated.get()) return;
        collector.onCollect(t);
    }

    @Override
    public void onTerminate(@Nullable Throwable e) {
        if (isTerminated.compareAndSet(false, true)) {
            collector.onTerminate(e);
        }
    }

    public boolean isTerminated() {
        return isTerminated.get();
    }
}
