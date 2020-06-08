package io.rxflow.flow.flow;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicInteger;

import io.rxflow.flow.callee.ArrayCallee;
import io.rxflow.flow.callee.Callee;
import io.rxflow.flow.caller.Collector;
import io.rxflow.flow.caller.CollectorCaller;
import io.rxflow.flow.caller.CollectorHelper;
import io.rxflow.func.Consumer;
import io.rxflow.func.Predicate;

/**
 * @author 7hens
 */
public abstract class RxFlow<T> {

    public abstract void collect(@NotNull Collector<T> collector);

    public interface Operator<Up, Dn> {
        @NotNull
        Collector<Up> apply(@NotNull Collector<Dn> collector) throws Throwable;
    }

    public static <T> RxFlow<T> of(@NotNull Callee<T> callee) {
        return new RxFlow<T>() {
            @Override
            public void collect(@NotNull Collector<T> collector) {
                callee.reply(new CollectorCaller<>(collector));
            }
        };
    }

    @SafeVarargs
    public static <T> RxFlow<T> just(T... items) {
        return of(ArrayCallee.of(items));
    }

    public <R> RxFlow<R> lift(@NotNull Operator<T, R> operator) {
        RxFlow<T> upFlow = this;
        return new RxFlow<R>() {
            @Override
            public void collect(@NotNull Collector<R> collector) {
                try {
                    upFlow.collect(new CollectorHelper<>(operator.apply((collector))));
                } catch (Throwable e) {
                    collector.onTerminate(e);
                }
            }
        };
    }

    public RxFlow<T> onEach(@NotNull Consumer<T> consumer) {
        return lift(collector -> new Collector<T>() {

            @Override
            public void onCollect(@Nullable T t) {
                try {
                    consumer.accept(t);
                    collector.onCollect(t);
                } catch (Throwable e) {
                    onTerminate(e);
                }
            }

            @Override
            public void onTerminate(@Nullable Throwable e) {
                collector.onTerminate(e);
            }
        });
    }

    public RxFlow<T> filter(@NotNull Predicate<T> predicate) {
        return lift(collector -> new Collector<T>() {
            @Override
            public void onCollect(@Nullable T t) {
                try {
                    if (predicate.test(t)) {
                        collector.onCollect(t);
                    }
                } catch (Throwable e) {
                    onTerminate(e);
                }
            }

            @Override
            public void onTerminate(@Nullable Throwable e) {
                collector.onTerminate(e);
            }
        });
    }

    public RxFlow<T> take(int limit) {
        AtomicInteger count = new AtomicInteger(limit);
        return lift(collector -> new Collector<T>() {
            @Override
            public void onCollect(@Nullable T t) {
                if (count.get() > 0) {
                    collector.onCollect(t);
                }
                if (count.decrementAndGet() == 0) {
                    onTerminate(null);
                }
            }

            @Override
            public void onTerminate(@Nullable Throwable e) {
                collector.onTerminate(e);
            }
        });
    }
}
