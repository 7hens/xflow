package io.xflow.flow;


import java.util.concurrent.Executors;

import io.xflow.flow.caller.Collector;
import io.xflow.func.Consumer;
import io.xflow.scheduler.RxScheduler;

/**
 * @author 7hens
 */
public class X {
    public static void log(String message) {
        System.out.println(message);
    }

    public static <T> Consumer<T> consumer(String name) {
        return it -> log("[" + name + "] (#" + threadName() + ") Consumer: " + it);
    }

    public static <T> Collector<T> collector(String name) {
        return new Collector<T>() {
            @Override
            public void onCollect(T t) {
                log("[" + name + "] (#" + threadName() + ") Collector.collect: " + t);
            }

            @Override
            public void onTerminate(Throwable e) {
                log("[" + name + "] (#" + threadName() + ") Collector.terminate: " + e);
                if (e != null) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    private static String threadName() {
        return Thread.currentThread().getName();
    }

    public static RxScheduler scheduler(String name) {
        return RxScheduler.fromExecutor(Executors.newFixedThreadPool(2, runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName(name);
            return thread;
        }));
    }

    public static void delay(long timeMs) {
        try {
            Thread.sleep(timeMs);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}