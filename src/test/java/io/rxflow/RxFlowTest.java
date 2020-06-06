package io.rxflow;


import org.junit.Test;
import org.junit.experimental.theories.suppliers.TestedOn;

import java.util.Arrays;

import io.rxflow.collector.RxCollector;
import io.rxflow.flow.RxFlow;
import io.rxflow.func.Consumer;

/**
 * @author 7hens
 */
public class RxFlowTest {
    @Test
    public void create() {
        RxFlow.<Integer>create(emitter -> {
            emitter.emit(1);
            emitter.emit(2);
            emitter.terminate(null);
        }).collect(logCollector("create"));
    }

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
                .collect();
    }

    @Test
    public void map() {
        RxFlow.just(1, 2, 3)
                .map(it -> it + 3)
                .collect(logCollector("map"));
    }

    @Test
    public void filter() {
        RxFlow.just(1, 2, 3, 4, 5, 6)
                .onEach(logConsumer(".before"))
                .filter(it -> it > 5)
                .collect(logCollector("filter"));
    }

    @Test
    public void any() {
        RxFlow.just(1, 2, 1, 2, 3)
                .any(it -> it == 1)
                .collect(logCollector("any"));
    }

    private <T> Consumer<T> logConsumer(String name) {
        return it -> System.out.println("[" + name + "] " + it);
    }

    private <T> RxCollector<T> logCollector(String name) {
        return new RxCollector<T>() {
            @Override
            public void onCollect(T t) throws Throwable {
                System.out.println("[" + name + "] onEach: " + t);
            }

            @Override
            public void onTerminate(Throwable e) throws Throwable {
                System.out.println("[" + name + "] onTerminate:  " + e);
            }
        };
    }
}