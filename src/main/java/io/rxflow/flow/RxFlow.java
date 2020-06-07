package io.rxflow.flow;

import io.rxflow.flow.callee.ArrayCallee;
import io.rxflow.flow.callee.Callee;
import io.rxflow.flow.callee.CalleeTransform;
import io.rxflow.flow.callee.IteratorCallee;
import io.rxflow.flow.caller.Caller;
import io.rxflow.flow.caller.ContinueCaller;
import io.rxflow.flow.caller.Collector;
import io.rxflow.flow.reply.Reply;
import io.rxflow.flow.reply.ReplyWrapper;
import io.rxflow.func.Consumer;
import io.rxflow.func.Func;
import io.rxflow.func.Predicate;

public abstract class RxFlow<T> {
    protected abstract Callee<T> callee();

    public void collect(Collector<T> collector) {
        callee().reply(ContinueCaller.create(collector));
    }

    public static <T> RxFlow<T> of(Callee<T> callee) {
        return new RxFlow<T>() {
            @Override
            protected Callee<T> callee() {
                return callee;
            }
        };
    }

    @SafeVarargs
    public static <T> RxFlow<T> just(T... items) {
        return of(ArrayCallee.of(items));
    }

    public static <T> RxFlow<T> of(Iterable<T> iterable) {
        return of(IteratorCallee.of(iterable.iterator()));
    }

    public interface Operator<Up, Dn> {
        void apply(Reply<Up> reply, Caller<Dn> caller) throws Throwable;
    }

    private static void log(String text) {
        System.out.println(text);
    }

    public <R> RxFlow<R> transform(final Operator<T, R> operator) {
        final RxFlow<T> upFlow = this;
        return of(new CalleeTransform<>(upFlow.callee(), operator));
    }

    public <R> RxFlow<R> map(Func<T, R> mapper) {
        return transform((reply, caller) -> {
            caller.receive(reply.map(mapper));
        });
    }

    public RxFlow<T> onEach(Consumer<T> onEach) {
        return transform(new Operator<T, T>() {
            @Override
            public void apply(Reply<T> reply, Caller<T> caller) throws Throwable {
                if (!reply.over()) {
                    onEach.accept(reply.value());
                }
                caller.receive(reply);
            }
        });
    }

    public RxFlow<T> filter(Predicate<T> predicate) {
        final RxFlow<T> upFlow = this;
        return transform(new Operator<T, T>() {
            @Override
            public void apply(Reply<T> reply, Caller<T> caller) throws Throwable {
                if (!reply.over()) {
                    if (predicate.test(reply.value())) {
                        caller.receive(reply);
                    } else {
                        reply.callee().reply(caller);
                    }
                } else {
                    caller.receive(reply);
                }
            }
        });
    }
}
