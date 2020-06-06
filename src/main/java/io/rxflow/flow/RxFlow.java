package io.rxflow.flow;

import io.rxflow.collector.RxCollector;
import io.rxflow.collector.RxCollectors;
import io.rxflow.func.Cancellable;
import io.rxflow.func.Consumer;
import io.rxflow.func.Func;
import io.rxflow.func.Predicate;

/**
 * @author 7hens
 */
@SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
public abstract class RxFlow<T> {
    public interface Emitter<T> {
        void emit(T item);

        void terminate(Throwable e);

        boolean isTerminated();
    }

    public interface Creator<T> {
        void create(Emitter<? super T> emitter) throws Throwable;
    }

    public interface Converter<Up, Dn> {
        Dn apply(RxFlow<Up> upStream);
    }

    public interface Operator<Up, Dn> {
        RxCollector<? super Up> apply(RxCollector<? super Dn> downStream);
    }

    public abstract Cancellable collect(RxCollector<? super T> collector);

    public Cancellable collect() {
        return collect(RxCollectors.empty());
    }

    public static <T> RxFlow<T> create(Creator<T> creator) {
        return new Create<>(creator);
    }

    @SafeVarargs
    public static <T> RxFlow<T> just(T... items) {
        return create(new CreateJust<>(items));
    }

    public static <T> RxFlow<T> of(Iterable<? extends T> iterable) {
        return create(new CreateOf<>(iterable));
    }

    public <R> R to(Converter<T, ? extends R> converter) {
        return converter.apply(this);
    }

    public <R> RxFlow<R> lift(Operator<? super T, ? extends R> operator) {
        return to(new OpLift<>(operator));
    }

    public RxFlow<T> filter(Predicate<? super T> predicate) {
        return lift(new OpFilter<>(predicate));
    }

    public RxFlow<Boolean> any(Predicate<? super T> predicate) {
        return lift(new OpAny<>(predicate));
    }

    public RxFlow<T> onEach(Consumer<? super T> consumer) {
        return lift(new OpOnEach<>(consumer));
    }

    public <R> RxFlow<R> map(Func<? super T,? extends R> mapper) {
        return lift(new OpMap<>(mapper));
    }
}
