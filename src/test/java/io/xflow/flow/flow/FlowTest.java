package io.xflow.flow.flow;

import org.junit.Test;

import io.xflow.flow.X;

/**
 * @author 7hens
 */
public class FlowTest {
    @Test
    public void just() {
        Flow.just(1, 2, 3).collect(X.collector("just"));
    }

    @Test
    public void filter() {
        Flow.just(1, 2, 3, 4, 5, 6)
                .onEach(X.consumer("filter"))
                .filter(it -> it == 3)
                .collect(X.collector("filter"));
    }

    @Test
    public void take() {
        Flow.just(1, 2, 3, 4, 5, 6)
                .onEach(X.consumer("take"))
                .take(2)
                .collect(X.collector("take"));
    }

    @Test
    public void flowOn() {
        Flow.just(1, 2, 3, 4, 5)
                .onEach(X.consumer("flowOn.A"))
                .flowOn(X.scheduler("A"))
                .onEach(X.consumer("flowOn.B"))
                .flowOn(X.scheduler("B"))
                .collect(X.collector("flowOn.C"));
        X.delay(3 * 1000);
    }
}
