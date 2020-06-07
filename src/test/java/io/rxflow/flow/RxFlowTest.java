package io.rxflow.flow;


import org.junit.Test;

import java.util.Arrays;

import io.rxflow.flow.caller.Collector;
import io.rxflow.func.Consumer;

/**
 * @author 7hens
 */
public class RxFlowTest {

    @Test
    public void just() {
        RxFlow.just(1, 2, 3).collect(logCollector("just"));
    }

    @Test
    public void of() {
        RxFlow.of(Arrays.asList(1, 2, 3)).collect(logCollector("of"));
    }

    @Test
    public void onEach() {
        RxFlow.just(1, 2, 3)
                .onEach(logConsumer("onEach"))
                .collect(logCollector("onEach"));
    }

    @Test
    public void filter() {
        RxFlow.just(1, 2, 3, 4, 5, 6)
                .onEach(logConsumer(".before"))
                .filter(it -> it == 2)
                .collect(logCollector("filter"));
    }

    @Test
    public void map() {
        RxFlow.just(1, 2, 3, 4, 5)
                .map(it -> it + 10)
                .collect(logCollector("map"));
    }

    private <T> Consumer<T> logConsumer(String name) {
        return it -> System.out.println("[" + name + "] Consumer: " + it);
    }

    private <T> Collector<T> logCollector(String name) {
        return new Collector<T>() {
            @Override
            public void onCollect(T t) {
                System.out.println("[" + name + "] Collector.collect: " + t);
            }

            @Override
            public void onTerminate(Throwable e) {
                System.out.println("[" + name + "] Collector.terminate: " + e);
            }
        };
    }
}