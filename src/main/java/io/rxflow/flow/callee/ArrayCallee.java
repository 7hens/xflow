package io.rxflow.flow.callee;

import java.util.Iterator;

public abstract class ArrayCallee<T> extends IteratorCallee<T> implements Iterator<T> {
    private int index = 0;

    protected abstract T[] items();

    @Override
    protected Iterator<T> iterator() {
        return this;
    }

    @Override
    public boolean hasNext() {
        return index < items().length;
    }

    @Override
    public T next() {
        return items()[index++];
    }

    public static <T> ArrayCallee<T> of(T[] items) {
        return new ArrayCallee<T>() {
            @Override
            protected T[] items() {
                return items;
            }
        };
    }
}
