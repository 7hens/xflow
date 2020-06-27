package cn.thens.xflow.flow;


import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import cn.thens.xflow.func.Action1;
import cn.thens.xflow.func.Func0;

/**
 * @author 7hens
 */
final class FlowCreate {
    static <T> Flow<T> create(Action1<? super Emitter<? super T>> onStart) {
        return new AbstractFlow<T>() {
            @Override
            protected void onStart(CollectorEmitter<? super T> emitter) throws Throwable {
                onStart.invoke(emitter);
            }
        };
    }

    static <T> Flow<T> empty() {
        return new AbstractFlow<T>() {
            @Override
            protected void onStart(CollectorEmitter<? super T> emitter) {
                emitter.complete();
            }
        };
    }

    static <T> Flow<T> never() {
        return new AbstractFlow<T>() {
            @Override
            protected void onStart(CollectorEmitter<? super T> emitter) {
            }
        };
    }

    static <T> Flow<T> error(final Throwable e) {
        return new AbstractFlow<T>() {
            @Override
            protected void onStart(CollectorEmitter<? super T> emitter) {
                emitter.error(e);
            }
        };
    }

    static <T> Flow<T> defer(final Func0<Flow<T>> flowFactory) {
        return new AbstractFlow<T>() {
            @Override
            protected void onStart(CollectorEmitter<? super T> emitter) throws Throwable {
                flowFactory.invoke().collect(emitter);
            }
        };
    }

    static <T> Flow<T> fromArray(T[] elements) {
        return new AbstractFlow<T>() {
            @Override
            protected void onStart(CollectorEmitter<? super T> emitter) throws Throwable {
                for (T item : elements) {
                    emitter.data(item);
                }
                emitter.complete();
            }
        };
    }

    static <T> Flow<T> fromIterable(Iterable<T> iterable) {
        return new AbstractFlow<T>() {
            @Override
            protected void onStart(CollectorEmitter<? super T> emitter) throws Throwable {
                for (T item : iterable) {
                    emitter.data(item);
                }
                emitter.complete();
            }
        };
    }

    static Flow<Integer> range(int start, int end, int step) {
        if (step == 0) {
            throw new IllegalArgumentException("Step cannot be zero");
        }
        if ((end - start) * step < 0) {
            throw new IllegalArgumentException("Illegal range " + start + "~" + end + ", " + step);
        }
        return new AbstractFlow<Integer>() {
            @Override
            protected void onStart(CollectorEmitter<? super Integer> emitter) throws Throwable {
                if (end > start) {
                    for (int i = start; i <= end; i += step) {
                        emitter.data(i);
                    }
                } else {
                    for (int i = start; i >= end; i += step) {
                        emitter.data(i);
                    }
                }
                emitter.complete();
            }
        };
    }

    static Flow<Long> timer(long delay, TimeUnit unit) {
        return new AbstractFlow<Long>() {
            @Override
            protected void onStart(CollectorEmitter<? super Long> emitter) throws Throwable {
                emitter.scheduler().schedule(new Runnable() {
                    @Override
                    public void run() {
                        emitter.data(0L);
                        emitter.complete();
                    }
                }, delay, unit);
            }
        };
    }

    static Flow<Long> interval(long initialDelay, long period, TimeUnit unit) {
        return new AbstractFlow<Long>() {
            @Override
            protected void onStart(CollectorEmitter<? super Long> emitter) throws Throwable {
                final AtomicLong count = new AtomicLong(0);
                emitter.scheduler().schedulePeriodically(new Runnable() {
                    @Override
                    public void run() {
                        emitter.data(count.getAndIncrement());
                    }
                }, initialDelay, period, unit);
            }
        };
    }
}
