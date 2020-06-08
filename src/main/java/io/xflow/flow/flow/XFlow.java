package io.xflow.flow.flow;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicInteger;

import io.xflow.flow.callee.ArrayCallee;
import io.xflow.flow.callee.Callee;
import io.xflow.flow.caller.Collector;
import io.xflow.flow.reply.Reply;
import io.xflow.func.Consumer;
import io.xflow.func.Predicate;

/**
 * @author 7hens
 */
public abstract class XFlow<T> {
    public abstract void collect(@NotNull Collector<T> collector);

    public interface Operator<Up, Dn> {
        @NotNull
        Collector<Up> apply(@NotNull Collector<Dn> collector) throws Throwable;
    }

    public static <T> XFlow<T> of(@NotNull Callee<T> callee) {
        return new XFlow<T>() {
            @Override
            public void collect(@NotNull Collector<T> collector) {
                callee.reply(collector);
            }
        };
    }

    @SafeVarargs
    public static <T> XFlow<T> just(T... items) {
        return of(ArrayCallee.of(items));
    }

    public <R> XFlow<R> lift(@NotNull Operator<T, R> operator) {
        return new XFlowLift<>(this, operator);
    }

    public XFlow<T> onEach(@NotNull Consumer<T> consumer) {
        return lift(collector -> new Collector<T>() {

            @Override
            public void onCollect(@Nullable T t) {
                try {
                    consumer.accept(t);
                    collector.collect(t);
                } catch (Throwable e) {
                    onTerminate(e);
                }
            }

            @Override
            public void onTerminate(@Nullable Throwable e) {
                collector.terminate(e);
            }
        });
    }

    public XFlow<T> filter(@NotNull Predicate<T> predicate) {
        return lift(collector -> new Collector<T>() {
            @Override
            public void onCollect(@Nullable T t) {
                try {
                    if (predicate.test(t)) {
                        collector.collect(t);
                    }
                } catch (Throwable e) {
                    onTerminate(e);
                }
            }

            @Override
            public void onTerminate(@Nullable Throwable e) {
                collector.terminate(e);
            }
        });
    }

    public XFlow<T> take(int limit) {
        AtomicInteger count = new AtomicInteger(limit);
        return lift(collector -> new Collector<T>() {
            @Override
            public void onCollect(@Nullable T t) {
                if (count.get() > 0) {
                    collector.collect(t);
                }
                if (count.decrementAndGet() == 0) {
                    onTerminate(null);
                }
            }

            @Override
            public void onTerminate(@Nullable Throwable e) {
                collector.terminate(e);
            }
        });
    }

}
