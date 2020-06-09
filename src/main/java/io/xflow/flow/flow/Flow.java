package io.xflow.flow.flow;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicInteger;

import io.xflow.flow.callee.ArrayCallee;
import io.xflow.flow.callee.Callee;
import io.xflow.flow.caller.CallerEmitter;
import io.xflow.flow.caller.Collector;
import io.xflow.flow.caller.Emitter;
import io.xflow.flow.reply.Replies;
import io.xflow.func.Cancellable;
import io.xflow.func.Consumer;
import io.xflow.func.Predicate;
import io.xflow.scheduler.Scheduler;

/**
 * @author 7hens
 */
public abstract class Flow<T> {
    public abstract Cancellable collect(@NotNull Collector<T> collector);

    public interface Operator<Up, Dn> {
        @NotNull
        Collector<Up> apply(@NotNull Emitter<Dn> emitter) throws Throwable;
    }

    public static <T> Flow<T> of(@NotNull Callee<T> callee) {
        return new Flow<T>() {
            @Override
            public Cancellable collect(@NotNull Collector<T> collector) {
                CallerEmitter<T> caller = new CallerEmitter<>(collector);
                callee.reply(caller);
                return caller;
            }
        };
    }

    @SafeVarargs
    public static <T> Flow<T> just(T... items) {
        return of(ArrayCallee.of(items));
    }


    public static <T> Flow<T> error(Throwable e) {
        return of(caller -> caller.receive(Replies.of(e)));
    }

    public static <T> Flow<T> empty() {
        return error(null);
    }

    public static <T> Flow<T> never() {
        return of(caller -> {
        });
    }

    public <R> Flow<R> lift(@NotNull Operator<T, R> operator) {
        return new FlowLift<>(this, operator);
    }

    public Flow<T> onEach(@NotNull Consumer<T> consumer) {
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

    public Flow<T> filter(@NotNull Predicate<T> predicate) {
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

    public Flow<T> take(int limit) {
        if (limit == 0) {
            return empty();
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

    public Flow<T> flowOn(Scheduler scheduler) {
        Flow<T> upFlow = this;
        return new Flow<T>() {
            @Override
            public Cancellable collect(@NotNull Collector<T> collector) {
                return upFlow.collect(collector);
            }
        };
    }
}
