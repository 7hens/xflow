package io.rxflow.flow.callee;

import org.junit.Test;

import java.util.Arrays;

import io.rxflow.flow.X;

/**
 * @author 7hens
 */
public class CalleeTest {
    @Test
    public void just() {
        Callee.just(1, 2, 3).collect(X.collector("just"));
    }

    @Test
    public void of() {
        Callee.of(Arrays.asList(11, 12, 13)).collect(X.collector("of"));
    }

    @Test
    public void map() {
        Callee.just(1, 2, 3)
                .map(it -> it + 100)
                .collect(X.collector("map"));
    }

    @Test
    public void filter() {
        Callee.just(1, 2, 3, 4, 5, 6)
                .filter(it -> it == 3)
                .collect(X.collector("filter"));
    }
}
