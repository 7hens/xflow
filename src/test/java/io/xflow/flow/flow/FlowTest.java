package io.xflow.flow.flow;

import org.junit.Test;

import io.xflow.flow.X;

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
                .onEach(X.consumer("filter"))
                .filter(it -> it == 3)
                .onCollect(X.collector("filter"))
                .collect();
    }

    @Test
    public void take() {
        Flow.just(1, 2, 3, 4, 5, 6)
                .onEach(X.consumer("take"))
                .take(2)
                .onCollect(X.collector("take"))
//                .flowOn(X.scheduler("A"))
                .collect();
        X.delay(2 * 1000);
    }

    @Test
    public void flowOn() {
        Flow.just(1, 2, 3, 4, 5)
                .onEach(X.consumer("flowOn.A"))
                .flowOn(X.scheduler("A"))
                .onEach(X.consumer("flowOn.B"))
                .flowOn(X.scheduler("B"))
                .onCollect(X.collector("flowOn.C"))
                .flowOn(X.scheduler("C"))
                .collect();
        X.delay(2 * 1000);
    }
}
