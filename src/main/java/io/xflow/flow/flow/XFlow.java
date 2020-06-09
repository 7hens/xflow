package io.xflow.flow.flow;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicInteger;

import io.xflow.flow.callee.ArrayCallee;
import io.xflow.flow.callee.Callee;
import io.xflow.flow.caller.Collector;
import io.xflow.flow.caller.CallerEmitter;
import io.xflow.flow.caller.Emitter;
import io.xflow.func.Cancellable;
import io.xflow.func.Consumer;
import io.xflow.func.Predicate;

/**
 * @author 7hens
 */
public abstract class XFlow<T> {
    public abstract Cancellable collect(@NotNull Collector<T> collector);

    public interface Operator<Up, Dn> {
        @NotNull
        Collector<Up> apply(@NotNull Emitter<Dn> emitter) throws Throwable;
    }

    public static <T> XFlow<T> of(@NotNull Callee<T> callee) {
        return new XFlow<T>() {
            @Override
            public Cancellable collect(@NotNull Collector<T> collector) {
                CallerEmitter<T> caller = CallerEmitter.of(collector);
                callee.reply(caller);
                return caller;
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
        return lift(emitter -> new Collector<T>() {
            @Override
            public void onCollect(T t) {
                try {
                    consumer.accept(t);
                    emitter.emit(t);
                } catch (Throwable e) {
                    onTerminate(e);
                }
            }

            @Override
            public void onTerminate(@Nullable Throwable e) {
                emitter.over(e);
            }
        });
    }

    public XFlow<T> filter(@NotNull Predicate<T> predicate) {
        return lift(emitter -> new Collector<T>() {
            @Override
            public void onCollect(@Nullable T t) {
                try {
                    if (predicate.test(t)) {
                        emitter.emit(t);
                    }
                } catch (Throwable e) {
                    onTerminate(e);
                }
            }

            @Override
            public void onTerminate(@Nullable Throwable e) {
                emitter.over(e);
            }
        });
    }

    public XFlow<T> take(int limit) {
        if (limit == 0) {
            // empty()
        }
        AtomicInteger count = new AtomicInteger(limit);
        return lift(emitter -> new Collector<T>() {
            @Override
            public void onCollect(T t) {
                if (count.get() <= 0) return;
                int rest = count.decrementAndGet();
                emitter.emit(t);
                if (rest == 0) {
                    onTerminate(null);
                    emitter.cancel();
                }
            }

            @Override
            public void onTerminate(@Nullable Throwable e) {
                emitter.over(e);
            }
        });
    }
}
