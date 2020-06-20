package cn.thens.xflow;


import java.util.concurrent.Executors;

import io.xflow.cancellable.Cancellable;
import io.xflow.scheduler.Scheduler;

/**
 * @author 7hens
 */
@SuppressWarnings("ALL")
public class X {
    public static void log(String message) {
        System.out.println(message);
    }

    public static <T> Collector<T> collector(String name) {
        return new CollectorHelper<T>() {
            @Override
            protected void onStart(Cancellable cancellable) {
                super.onStart(cancellable);
                log("onStart");
            }

            @Override
            protected void onEach(T data) {
                super.onEach(data);
                log("onEach: " + data);
            }

            @Override
            protected void onComplete() {
                super.onComplete();
                log("onComplete");
            }

            @Override
            protected void onError(Throwable e) {
                super.onError(e);
                log("onError: " + e.getClass().getName());
            }

            private void log(String message) {
                String threadName = Thread.currentThread().getName();
                log("@Flow:[" + name + "] (" + threadName + ") " + message);
            }
        };
    }

    private static String threadName() {
        return Thread.currentThread().getName();
    }

    public static Scheduler scheduler(String name) {
        return Scheduler.from(Executors.newFixedThreadPool(8, runnable -> {
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