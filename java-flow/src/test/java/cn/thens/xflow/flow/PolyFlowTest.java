package cn.thens.xflow.flow;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import cn.thens.xflow.TestX;
import cn.thens.xflow.func.Func1;

/**
 * @author 7hens
 */
public class PolyFlowTest {
    private void flat(Func1<PolyFlow<String>, Flow<String>> converter) {
        Flow.interval(2, TimeUnit.SECONDS)
                .take(3)
                .onCollect(TestX.collector("A"))
                .flowOn(TestX.scheduler("a"))
                .polyMap(it -> Flow.interval(1, TimeUnit.SECONDS)
                        .take(5)
                        .map(t -> {
                            if (t == 4) throw new RuntimeException();
                            return it + "." + t;
                        }))
                .polyTo(converter)
                .onCollect(TestX.collector("B"))
                .to(TestX.collect());
    }


    @Test
    public void delayErrors() {
        flat(flow -> flow.delayErrors().flatMerge());
    }

    @Test
    public void flatZip() {
        flat(flow -> flow.flatZip().map(it -> String.join(",", it)));
    }

    @Test
    public void flatSwitch() {
        flat(PolyFlow::flatSwitch);
    }

    @Test
    public void flatMerge() {
        flat(PolyFlow::flatMerge);
    }

    @Test
    public void flatConcat() {
        flat(PolyFlow::flatConcat);
    }
}
