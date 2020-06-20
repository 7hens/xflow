package cn.thens.xflow.flow;

import java.util.concurrent.TimeUnit;

/**
 * @author 7hens
 */
public class FlowTimer extends AbstractFlow<Long> {
    private final long delay;
    private final TimeUnit unit;

    FlowTimer(long delay, TimeUnit unit) {
        this.delay = delay;
        this.unit = unit;
    }

    @Override
    protected void onStart(CollectorEmitter<Long> emitter) {
        emitter.scheduler().schedule(new Runnable() {
            @Override
            public void run() {
                emitter.data(0L);
                emitter.complete();
            }
        }, delay, unit);
    }
}
