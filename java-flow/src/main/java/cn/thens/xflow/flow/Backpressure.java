package cn.thens.xflow.flow;

import java.util.LinkedList;

import cn.thens.xflow.func.Function;
import cn.thens.xflow.func.Functions;

@SuppressWarnings("WeakerAccess")
public abstract class Backpressure<T> {
    abstract void apply(LinkedList<T> queue) throws Throwable;

    public final Backpressure<T> catchError(Function<? super Throwable, ? extends Backpressure<T>> catchError) {
        Backpressure<T> self = this;
        return new Backpressure<T>() {
            @Override
            void apply(LinkedList<T> queue) throws Throwable {
                try {
                    self.apply(queue);
                } catch (Throwable e) {
                    catchError.apply(e).apply(queue);
                }
            }
        };
    }

    public final Backpressure<T> catchError(Backpressure<T> backpressure) {
        return catchError(Functions.always(backpressure));
    }

    public final Backpressure<T> dropAll() {
        return catchError(new Backpressure<T>() {
            @Override
            void apply(LinkedList<T> queue) throws Throwable {
                queue.clear();
            }
        });
    }

    public final Backpressure<T> dropLatest() {
        return catchError(new Backpressure<T>() {
            @Override
            void apply(LinkedList<T> queue) throws Throwable {
                queue.removeLast();
            }
        });
    }

    public final Backpressure<T> dropOldest() {
        return catchError(new Backpressure<T>() {
            @Override
            void apply(LinkedList<T> queue) throws Throwable {
                queue.removeFirst();
            }
        });
    }

    public static <T> Backpressure<T> buffer(int capacity) {
        return new Backpressure<T>() {
            @Override
            void apply(LinkedList<T> queue) {
                if (queue.size() > capacity) {
                    throw new FullBufferException();
                }
            }
        };
    }

    public static <T> Backpressure<T> error() {
        return new Backpressure<T>() {
            @Override
            void apply(LinkedList<T> queue) throws Throwable {
                throw new FullBufferException();
            }
        };
    }

    public static <T> Backpressure<T> none() {
        return new Backpressure<T>() {
            @Override
            void apply(LinkedList<T> queue) throws Throwable {
            }
        };
    }

    public static class FullBufferException extends RuntimeException {
    }
}
