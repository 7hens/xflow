package io.rxflow.flow.flow;

import org.junit.Test;

import io.rxflow.flow.X;

/**
 * @author 7hens
 */
public class RxFlowTest {
    @Test
    public void just() {
        RxFlow.just(1, 2, 3).collect(X.collector("just"));
    }

    @Test
    public void filter() {
        RxFlow.just(1, 2, 3, 4, 5, 6)
                .onEach(X.consumer("filter"))
                .filter(it -> it == 3)
                .collect(X.collector("filter"));
    }

    @Test
    public void take() {
        RxFlow.just(1, 2, 3, 4, 5, 6)
                .onEach(X.consumer("take"))
                .take(2)
                .collect(X.collector("take"));
    }
}
