package cn.thens.xflow.flow;

import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import cn.thens.xflow.TestX;

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
                .take(5)
                .onCollect(TestX.collector("B"))
                .to(TestX.collect());
    }

    @Test
    public void bufferFilter() {
        Flow.interval(1, TimeUnit.SECONDS)
                .buffer(3)
                .take(5)
                .onCollect(TestX.collector("B"))
                .to(TestX.collect());
    }

    @Test
    public void throttleFirst() {
        Flow.interval(100, TimeUnit.MILLISECONDS)
//                .onCollect(TestX.collector("A"))
                .throttleFirst(Flow.timer(170, TimeUnit.MILLISECONDS))
                .onCollect(TestX.collector("B"))
                .take(3)
                .to(TestX.collect());
    }

    @Test
    public void throttleLast() {
        AtomicLong count = new AtomicLong();
        final long startTime = System.currentTimeMillis();
        Flow.just(1, 1, 3, 1, 1, 3, 1, 1, 3)
                .map(it -> Flow.timer(it, TimeUnit.SECONDS))
                .transform(FlowX.flatConcat())
//                .onCollect(TestX.collector("A"))
                .map(it -> count.getAndIncrement())
                .map(it -> it + " " + ((System.currentTimeMillis() - startTime) / 1000) + "s")
                .throttleLast(Flow.timer(2, TimeUnit.SECONDS))
                .onCollect(TestX.collector("B"))
                .take(3)
                .to(TestX.collect());
    }
}
