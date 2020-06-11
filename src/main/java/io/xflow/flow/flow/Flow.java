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
import io.xflow.func.Action;
import io.xflow.func.Cancellable;
import io.xflow.func.Consumer;
import io.xflow.func.Functions;
import io.xflow.func.Predicate;
import io.xflow.scheduler.Scheduler;
import io.xflow.scheduler.Schedulers;

/**
 * @author 7hens
 */
@SuppressWarnings({"UnusedReturnValue", "WeakerAccess", "unused"})
public abstract class Flow<T> {
    protected abstract Cancellable collect(@NotNull Scheduler scheduler, @NotNull Collector<T> collector);

    public Cancellable collect() {
        return collect(Schedulers.unconfined(), new Collector<T>() {
            @Override
            public void onEach(T t, Action action) {
                try {
                    action.run();
                } catch (Throwable throwable) {
                    throw new RuntimeException(throwable);
                }
            }

            @Override
            public void onTerminate(@Nullable Throwable e, Action action) {
                try {
                    action.run();
                } catch (Throwable throwable) {
                    throw new RuntimeException(throwable);
                }
                if (e != null) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public interface Operator<Up, Dn> {
        @NotNull
        Collector<Up> apply(@NotNull Emitter<Dn> emitter) throws Throwable;
    }

    public static <T> Flow<T> of(@NotNull Callee<T> callee) {
        return new Flow<T>() {
            @Override
            public Cancellable collect(@NotNull Scheduler scheduler, @NotNull Collector<T> collector) {
                CallerEmitter<T> caller = new CallerEmitter<>(scheduler, collector);
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
            public void onEach(T t, Action action) {
                try {
                    consumer.accept(t);
                    emitter.emit(t, action);
                } catch (Throwable e) {
                    onTerminate(e, Functions.emptyAction());
                }
            }

            @Override
            public void onTerminate(@Nullable Throwable e, Action action) {
                emitter.over(e, action);
            }
        });
    }

    public Flow<T> onCollect(@NotNull Collector<T> collector) {
        return lift(emitter -> new Collector<T>() {
            @Override
            public void onEach(T t, Action action) {
                collector.onEach(t, Functions.emptyAction());
                emitter.emit(t, action);
            }

            @Override
            public void onTerminate(@Nullable Throwable e, Action action) {
                collector.onTerminate(e, Functions.emptyAction());
                emitter.over(e, action);
            }
        });
    }

    public Flow<T> filter(@NotNull Predicate<T> predicate) {
        return lift(emitter -> new Collector<T>() {
            @Override
            public void onEach(@Nullable T t, Action action) {
                try {
                    if (predicate.test(t)) {
                        emitter.emit(t, action);
                    } else {
                        action.run();
                    }
                } catch (Throwable e) {
                    onTerminate(e, Functions.emptyAction());
                }
            }

            @Override
            public void onTerminate(@Nullable Throwable e, Action action) {
                emitter.over(e, action);
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
            public void onEach(T t, Action action) {
                if (count.get() <= 0) return;
                int rest = count.decrementAndGet();
                emitter.emit(t, () -> {
                    if (rest == 0) {
                        onTerminate(null, emitter::cancel);
                    } else {
                        action.run();
                    }
                });
            }

            @Override
            public void onTerminate(@Nullable Throwable e, Action action) {
                emitter.over(e, action);
            }
        });
    }

    public Flow<T> lazy() {
        return new Flow<T>() {
            @Override
            protected Cancellable collect(@NotNull Scheduler scheduler, @NotNull Collector<T> collector) {
                return null;
            }
        };
    }

    public Flow<T> flowOn(Scheduler scheduler) {
        Flow<T> upFlow = this;
        Scheduler upScheduler = scheduler;
        return new Flow<T>() {
            @Override
            protected Cancellable collect(@NotNull Scheduler scheduler, @NotNull Collector<T> collector) {
                return upFlow.collect(upScheduler, new CallerEmitter<>(scheduler, collector).collector());
            }
        };
    }
}
