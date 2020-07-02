package cn.thens.xflow.flow;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import cn.thens.xflow.TestX;

/**
 * @author 7hens
 */
public class FlowEmitterTest {
    @Test
    public void publish() {
        test(FlowEmitter.publish());
    }

    @Test
    public void behavior() {
        test(FlowEmitter.behavior());
        test(FlowEmitter.behavior(32L));
    }

    @Test
    public void replay() {
        test(FlowEmitter.replay());
    }

    private void test(FlowEmitter<Long> emitter) {
        emitter.data(1L);
        emitter.data(2L);
        emitter.data(3L);
//        emitter.complete();

        Flow.interval(100, TimeUnit.MILLISECONDS)
                .onEach(it -> emitter.data(it + 4))
                .onTerminate(it -> emitter.complete())
                .take(5)
                .collect();

        emitter.asFlow()
                .onCollect(TestX.collector("A"))
                .to(TestX.collect());
    }
}
