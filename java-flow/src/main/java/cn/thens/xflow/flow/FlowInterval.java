package cn.thens.xflow.flow;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author 7hens
 */
public class FlowInterval extends AbstractFlow<Long> {
    private final long initialDelay;
    private final long period;
    private final TimeUnit unit;

    FlowInterval(long initialDelay, long period, TimeUnit unit) {
        this.initialDelay = initialDelay;
        this.period = period;
        this.unit = unit;
    }

    @Override
    protected void onStart(CollectorEmitter<Long> emitter) {
        final AtomicLong count = new AtomicLong(0);
        emitter.scheduler().schedulePeriodically(new Runnable() {
            @Override
            public void run() {
                emitter.data(count.getAndIncrement());
            }
        }, initialDelay, period, unit);
    }
}
