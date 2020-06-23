package cn.thens.xflow;


import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import cn.thens.xflow.cancellable.Cancellable;
import cn.thens.xflow.flow.Collector;
import cn.thens.xflow.flow.CollectorHelper;
import cn.thens.xflow.flow.Flow;
import cn.thens.xflow.func.Func1;
import cn.thens.xflow.scheduler.Scheduler;
import cn.thens.xflow.scheduler.Schedulers;


/**
 * @author 7hens
 */
@SuppressWarnings("ALL")
public class X {
    public static void log(String message) {
        String threadName = Thread.currentThread().getName();
        System.out.println("(" + threadName + ") " + message);
    }

    public static <T> Collector<T> collector(String name) {
        return new CollectorHelper<T>() {
            @Override
            protected void onStart(Cancellable cancellable) throws Throwable {
                super.onStart(cancellable);
                log("onStart");
            }

            @Override
            protected void onEach(T data) throws Throwable {
                super.onEach(data);
                log("onEach: " + data);
            }

            @Override
            protected void onComplete() throws Throwable {
                super.onComplete();
                log("onComplete");
            }

            @Override
            protected void onError(Throwable e) throws Throwable {
                super.onError(e);
                log("onError: " + e.getClass().getName());
            }

            private void log(String message) {
                X.log("[" + name + "] " + message);
            }
        };
    }

    private static String threadName() {
        return Thread.currentThread().getName();
    }

    public static Scheduler scheduler(String name) {
        final AtomicLong threadCount = new AtomicLong();
        return Schedulers.from(Executors.newFixedThreadPool(8, runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName(name + "-" + threadCount.incrementAndGet());
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

    public static <T> Func1<Flow<T>, Void> collect() {
        return new Func1<Flow<T>, Void>() {
            @Override
            public Void invoke(Flow<T> flow) throws Throwable {
                try {
                    CountDownLatch latch = new CountDownLatch(1);
                    flow.onCollect(new CollectorHelper<T>()
                            .onTerminate(it -> latch.countDown()))
                            .collect();
                    latch.await();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
    }
}