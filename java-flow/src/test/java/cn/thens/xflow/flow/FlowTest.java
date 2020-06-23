package cn.thens.xflow.flow;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import cn.thens.xflow.X;
import cn.thens.xflow.scheduler.Schedulers;

/**
 * @author 7hens
 */
public class FlowTest {

    @Test
    public void delayErrors() {
        Flow.interval(2, TimeUnit.SECONDS)
                .take(3)
                .onCollect(X.collector("A"))
                .flowOn(X.scheduler("a"))
                .map(it -> Flow.interval(1, TimeUnit.SECONDS)
                        .take(3)
                        .map(t -> {
                            if (t == 2) throw new RuntimeException();
                            return it + "." + t;
                        }))
                .transform(FlowX.delayErrors())
                .transform(FlowX.flatMerge())
                .onCollect(X.collector("B"))
                .to(X.collect());
    }

    @Test
    public void flatZip() {
        Flow.interval(2, TimeUnit.SECONDS)
                .take(3)
                .onCollect(X.collector("A"))
                .flowOn(X.scheduler("a"))
                .map(it -> Flow.interval(1, TimeUnit.SECONDS).take(3).map(c -> it + "." + c))
                .transform(FlowX.flatZip())
                .onCollect(X.collector("B"))
                .to(X.collect());
    }

    @Test
    public void flatSwitch() {
        Flow.interval(2, TimeUnit.SECONDS)
                .take(3)
                .onCollect(X.collector("A"))
                .flowOn(X.scheduler("a"))
                .map(it -> Flow.interval(1, TimeUnit.SECONDS).take(3).map(c -> it + "." + c))
                .transform(FlowX.flatSwitch())
                .onCollect(X.collector("B"))
                .to(X.collect());
    }

    @Test
    public void flatMerge() {
        Flow.interval(2, TimeUnit.SECONDS)
                .take(3)
                .onCollect(X.collector("A"))
                .flowOn(X.scheduler("a"))
                .map(it -> Flow.interval(1, TimeUnit.SECONDS).take(3).map(c -> it + "." + c))
                .transform(FlowX.flatMerge())
                .onCollect(X.collector("B"))
                .to(X.collect());
    }

    @Test
    public void flatConcat() {
        Flow.interval(2, TimeUnit.SECONDS)
                .take(3)
                .onCollect(X.collector("A"))
                .flowOn(X.scheduler("a"))
                .map(it -> Flow.interval(1, TimeUnit.SECONDS).take(3).map(c -> it + "." + c))
                .transform(FlowX.flatConcat())
                .onCollect(X.collector("B"))
                .to(X.collect());
    }

    @Test
    public void take() {
        Flow.interval(1, TimeUnit.SECONDS)
                .onCollect(X.collector("A"))
                .take(2)
                .map(it -> it + "..")
                .onCollect(X.collector("B"))
                .to(X.collect());
    }

    @Test
    public void range() {
        Flow.range(10, 1)
                .onCollect(X.collector("A"))
                .to(X.collect());
    }

    @Test
    public void timeout() {
        Flow.timer(2, TimeUnit.SECONDS)
                .onCollect(X.collector("A"))
                .timeout(1, TimeUnit.SECONDS)
                .onCollect(X.collector("B"))
                .to(X.collect());
    }

    @Test
    public void flowOn() {
        Flow.<String>create(emitter -> {
            String threadName = Thread.currentThread().getName();
            X.log("emitter (" + threadName + ")");

            emitter.data("hello");
            emitter.data("world");
            emitter.complete();
        })/////////
                .onCollect(X.collector("A"))
                .flowOn(X.scheduler("a"))
                .map(it -> it + "..")
                .onCollect(X.collector("B"))
                .flowOn(X.scheduler("b"))
                .to(X.collect());
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
                .onCollect(X.collector("B"))
                .to(X.collect());
    }

    private void readMe() {
        Flow.just(1, 2, 3, 4, 5)
                .take(3)
                .map(it -> Flow.just(it + 10, it + 20))
                .transform(FlowX.delayErrors())
                .transform(FlowX.flatMerge())
                .onCollect(new CollectorHelper<Integer>() {
                    @Override
                    protected void onEach(Integer s) {
                        System.out.println(s);
                    }

                    @Override
                    protected void onComplete() {
                    }
                })
                .flowOn(Schedulers.io())
                .collect();
    }
}
