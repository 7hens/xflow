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
        throttle(FlowX.pipe(it -> it.throttleFirst(Flow.timer(2, TimeUnit.SECONDS))));
    }

    @Test
    public void throttleLast() {
        throttle(FlowX.pipe(it -> it.throttleLast(Flow.timer(2, TimeUnit.SECONDS))));
    }

    private void throttle(FlowOperator<String, String> operator) {
        AtomicLong count = new AtomicLong(0);
        final long startTime = System.currentTimeMillis();
        Flow.just(1, 1, 3)
                .mapToFlow(it -> Flow.timer(it, TimeUnit.SECONDS))
                .flatConcat()
                .repeat()
                .map(it -> {
                    long elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000;
                    return count.incrementAndGet() + " " + elapsedSeconds + "s";
                })
                .onCollect(TestX.collector("A"))
                .transform(operator)
                .onCollect(TestX.collector("B"))
                .take(3)
                .to(TestX.collect());
    }

    @Test
    public void pipe() {
        Flow.just(1, 2, 3)
                .onCollect(TestX.collector("A"))
                .transform(FlowX.pipe(it -> it.map(i -> i + 10)))
                .onCollect(TestX.collector("B"))
                .to(TestX.collect());
    }

    private void backpressure(Backpressure<Long> backpressure) {
        Flow.interval(100, TimeUnit.MILLISECONDS)
//                .onCollect(TestX.collector("A"))
                .onBackpressure(backpressure)
                .map(it -> {
                    TestX.delay(400);
                    return it;
                })
                .onCollect(TestX.collector("B"))
                .take(6)
                .to(TestX.collect());
    }

    @Test
    public void onBackpressureDropOldest() {
        backpressure(Backpressure.<Long>buffer(2).dropOldest());
    }

    @Test
    public void onBackpressureDropLatest() {
        backpressure(Backpressure.<Long>buffer(2).dropLatest());
    }

    @Test
    public void onBackpressureDropAll() {
        backpressure(Backpressure.<Long>buffer(2).dropAll());
    }
}
