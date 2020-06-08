package io.rxflow.flow;


import io.rxflow.flow.caller.Collector;
import io.rxflow.func.Consumer;

/**
 * @author 7hens
 */
public class X {
    public static void log(String message) {
        System.out.println(message);
    }

    public static <T> Consumer<T> consumer(String name) {
        return it -> log("[" + name + "] Consumer: " + it);
    }

    public static <T> Collector<T> collector(String name) {
        return new Collector<T>() {
            @Override
            public void onCollect(T t) {
                log("[" + name + "] Collector.collect: " + t);
            }

            @Override
            public void onTerminate(Throwable e) {
                log("[" + name + "] Collector.terminate: " + e);
            }
        };
    }
}