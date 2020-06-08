package io.xflow.flow.callee;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

import io.xflow.flow.caller.Caller;
import io.xflow.flow.reply.Replies;

public abstract class IteratorCallee<T> implements Callee<T> {
    protected abstract Iterator<T> iterator();

    @Override
    public void reply(@NotNull Caller<T> caller) {
        if (iterator().hasNext()) {
            try {
                caller.receive(Replies.of(iterator().next(), this));
            } catch (Throwable e) {
                caller.receive(Replies.of(e));
            }
        } else {
            caller.receive(Replies.of(null));
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
