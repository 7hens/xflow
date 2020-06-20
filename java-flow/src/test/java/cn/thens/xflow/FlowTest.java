package cn.thens.xflow;

import org.junit.Test;

/**
 * @author 7hens
 */
public class FlowTest {
    @Test
    public void just() {
        Flow.just(1, 2, 3)
                .onCollect(X.collector("just"))
                .collect();
    }

    @Test
    public void filter() {
        Flow.just(1, 2, 3, 4, 5, 6)
                .onCollect(X.collector("filter A"))
                .filter(it -> it == 3)
                .onCollect(X.collector("filter B"))
                .collect();
    }

    @Test
    public void take() {
        Flow.just(1, 2, 3, 4, 5, 6)
                .onCollect(X.collector("take.before"))
                .flowOn(X.scheduler("A"))
                .take(2)
                .onCollect(X.collector("take"))
                .flowOn(X.scheduler("B"))
                .collect();
        X.delay(2 * 1000);
    }

    @Test
    public void flowOn() {
        Flow.just(1, 2, 3, 4, 5)
                .onCollect(X.collector("flowOn.A"))
                .flowOn(X.scheduler("A"))
                .onCollect(X.collector("flowOn.B"))
                .flowOn(X.scheduler("B"))
                .onCollect(X.collector("flowOn.C"))
                .flowOn(X.scheduler("C"))
                .collect();
        X.delay(2 * 1000);
    }
}
