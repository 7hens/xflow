package io.xflow.flow.reply;

import org.jetbrains.annotations.NotNull;

import java.util.NoSuchElementException;

import io.xflow.flow.callee.Callee;

public final class Replies {
    private Replies() {
    }

    public static <T> Reply<T> of(final T value, final Callee<T> callee) {
        return new Reply<T>() {
            @Override
            public boolean over() {
                return false;
            }

            @Override
            public Throwable error() {
                throw new NoSuchElementException("This is not a final replay");
            }

            @Override
            public T value() {
                return value;
            }

            @NotNull
            @Override
            public Callee<T> callee() {
                return callee;
            }
        };
    }

    public static <T> Reply<T> of(final Throwable e) {
        return new Reply<T>() {
            @Override
            public boolean over() {
                return true;
            }

            @Override
            public Throwable error() {
                return e;
            }

            @Override
            public T value() {
                throw new NoSuchElementException("This is a final replay");
            }

            @NotNull
            @Override
            public Callee<T> callee() {
                throw new NoSuchElementException("This is a final replay");
            }
        };
    }
}
