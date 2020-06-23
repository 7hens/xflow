package cn.thens.xflow.flow;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import cn.thens.xflow.TestX;

/**
 * @author 7hens
 */
public class FlowXTest {
    private void flat(Flow.Operator<Flow<String>, String> operator) {
        Flow.interval(2, TimeUnit.SECONDS)
                .take(3)
                .onCollect(TestX.collector("A"))
                .flowOn(TestX.scheduler("a"))
                .map(it -> Flow.interval(1, TimeUnit.SECONDS)
                        .take(3)
                        .map(t -> {
                            if (t == 2) throw new RuntimeException();
                            return it + "." + t;
                        }))
                .transform(operator)
                .onCollect(TestX.collector("B"))
                .to(TestX.collect());
    }


    @Test
    public void delayErrors() {
        flat(FlowX.<String>delayErrors().then(FlowX.flatMerge()));
    }

    @Test
    public void flatZip() {
        flat(FlowX.flatZip());
    }

    @Test
    public void flatSwitch() {
        flat(FlowX.flatSwitch());
    }

    @Test
    public void flatMerge() {
        flat(FlowX.flatMerge());
    }

    @Test
    public void flatConcat() {
        flat(FlowX.flatConcat());
    }

    @Test
    public void transform() {
        Flow.just(1, 2, 3)
                .onCollect(TestX.collector("A"))
                .transform(FlowX.from(it -> it.map(i -> i + 10)))
                .onCollect(TestX.collector("B"))
                .to(TestX.collect());
    }
}
