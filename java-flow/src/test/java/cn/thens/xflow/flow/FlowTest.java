package cn.thens.xflow.flow;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import cn.thens.xflow.TestX;
import cn.thens.xflow.cancellable.Cancellable;

/**
 * @author 7hens
 */
public class FlowTest {
    @Test
    public void take() {
        Flow.interval(1, TimeUnit.SECONDS)
                .onCollect(TestX.collector("A"))
                .take(0)
                .map(it -> it + "..")
                .onCollect(TestX.collector("B"))
                .to(TestX.collect());
    }

    @Test
    public void takeUtil() {
        Flow.interval(1, TimeUnit.SECONDS)
                .takeUntil(3L)
                .onCollect(TestX.collector("A"))
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
                .onCollect(TestX.collector("A"))
                .retry(3)
                .onCollect(TestX.collector("B"))
                .to(TestX.collect());
    }

    @Test
    public void autoCancel() {
        Flow.interval(1, TimeUnit.SECONDS)
                .onCollect(TestX.collector("A"))
                .autoCancel(Flow.timer(2, TimeUnit.SECONDS))
                .onCollect(TestX.collector("B"))
                .to(TestX.collect());
    }

    @Test
    public void reduce() {
        Flow.interval(1, TimeUnit.SECONDS)
                .take(5)
                .onCollect(TestX.collector("A"))
                .reduce(Long::sum)
                .onCollect(TestX.collector("B"))
                .to(TestX.collect());

        Flow.interval(1, TimeUnit.SECONDS)
                .take(5)
                .onCollect(TestX.collector("C"))
                .reduce(100L, Long::sum)
                .onCollect(TestX.collector("D"))
                .to(TestX.collect());
    }

    @Test
    public void delay() {
        Flow.interval(1, TimeUnit.SECONDS)
                .onCollect(TestX.collector("A"))
                .take(3)
                .delay(Flow.timer(5, TimeUnit.SECONDS))
                .onCollect(TestX.collector("B"))
                .to(TestX.collect());
    }

    @Test
    public void delayStart() {
        Flow.interval(1, TimeUnit.SECONDS)
                .onCollect(TestX.collector("A"))
                .take(3)
                .delayStart(Flow.timer(5, TimeUnit.SECONDS))
                .onCollect(TestX.collector("B"))
                .to(TestX.collect());
    }

    @Test
    public void toList() {
        Flow.interval(1, TimeUnit.SECONDS)
                .take(3)
                .toList()
                .onCollect(TestX.collector("A"))
                .to(TestX.collect());
    }

    @Test
    public void buffer() {
        Flow.interval(1, TimeUnit.SECONDS)
                .buffer(Flow.timer(2, TimeUnit.SECONDS))
                .map(it -> it.toList().onCollect(TestX.collector("A")))
                .take(5)
                .transform(FlowX.flatMerge())
                .onCollect(TestX.collector("B"))
                .to(TestX.collect());
    }
}
