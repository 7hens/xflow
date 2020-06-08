package io.rxflow.flow.callee;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

import io.rxflow.flow.caller.Caller;
import io.rxflow.flow.reply.Reply;

public abstract class IteratorCallee<T> implements Callee<T> {
    protected abstract Iterator<T> iterator();

    @Override
    public void reply(@NotNull Caller<T> caller) {
        if (iterator().hasNext()) {
            try {
                caller.receive(Reply.of(iterator().next(), this));
            } catch (Throwable e) {
                caller.receive(Reply.of(e));
            }
        } else {
            caller.receive(Reply.of(null));
        }
    }

    public static <T> IteratorCallee<T> of(Iterator<T> iterator) {
        return new IteratorCallee<T>() {
            @Override
            protected Iterator<T> iterator() {
                return iterator;
            }
        };
    }
}
