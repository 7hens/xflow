package io.rxflow.flow.callee;

import io.rxflow.flow.caller.Caller;
import io.rxflow.func.Func;
import io.rxflow.func.Predicate;

public abstract class Callee<T> {
    public abstract void reply(Caller<T> caller);

    @SafeVarargs
    public static <T> Callee<T> just(T... items) {
        return ArrayCallee.of(items);
    }

    public static <T> Callee<T> of(Iterable<T> iterable) {
        return IteratorCallee.of(iterable.iterator());
    }

    public <R> Callee<R> transform(CalleeOperator<T, R> operator) {
        return new CalleeTransform<>(this, operator);
    }

    public <R> Callee<R> map(Func<T, R> mapper) {
        return transform((reply, caller) -> {
            caller.receive(reply.map(mapper));
        });
    }

    public Callee<T> filter(Predicate<T> predicate) {
        return transform((reply, caller) -> {
            if (!reply.over()) {
                if (predicate.test(reply.value())) {
                    caller.receive(reply);
                }
                reply.callee().reply(caller);
            } else {
                caller.receive(reply);
            }
        });
    }
}
