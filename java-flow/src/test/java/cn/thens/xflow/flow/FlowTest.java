package cn.thens.xflow.flow;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import cn.thens.xflow.TestX;

/**
 * @author 7hens
 */
public class FlowTest {
    @Test
    public void take() {
        Flow.interval(1, TimeUnit.SECONDS)
                .onCollect(TestX.collector("A"))
                .take(2)
                .map(it -> it + "..")
                .onCollect(TestX.collector("B"))
                .to(TestX.collect());
    }

    @Test
    public void range() {
        Flow.range(10, 1)
                .onCollect(TestX.collector("A"))
                .to(TestX.collect());
    }

    @Test
    public void timeout() {
        Flow.timer(2, TimeUnit.SECONDS)
                .onCollect(TestX.collector("A"))
                .timeout(1, TimeUnit.SECONDS)
                .onCollect(TestX.collector("B"))
                .to(TestX.collect());
    }

    @Test
    public void flowOn() {
        Flow.<String>create(emitter -> {
            String threadName = Thread.currentThread().getName();
            TestX.logger().log("emitter (" + threadName + ")");

            emitter.data("hello");
            emitter.data("world");
            emitter.complete();
        })/////////
                .onCollect(TestX.collector("A"))
                .flowOn(TestX.scheduler("a"))
                .map(it -> it + "..")
                .onCollect(TestX.collector("B"))
                .flowOn(TestX.scheduler("b"))
                .to(TestX.collect());
    }

    @Test
    public void retry() {
        Flow.range(1, 10)
                .map(it -> {
                    if (it > 1) throw new Exception();
                    return it;
                })
//                .onCollect(x.collector("A"))
                .retry(3)
                .onCollect(TestX.collector("B"))
                .to(TestX.collect());
    }
}
