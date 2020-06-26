package cn.thens.xflow.flow;

import java.util.LinkedList;

@SuppressWarnings("WeakerAccess")
public abstract class Backpressure<T> {
    abstract void apply(LinkedList<T> queue) throws Throwable;

    public static <T> Backpressure<T> dropAll(int capacity) {
        return new Backpressure<T>() {
            @Override
            void apply(LinkedList<T> queue) {
                if (queue.size() > capacity) {
                    queue.clear();
                }
            }
        };
    }

    public static <T> Backpressure<T> error(int capacity) {
        return new Backpressure<T>() {
            @Override
            void apply(LinkedList<T> queue) {
                if (queue.size() > capacity) {
                    throw new FullBufferException();
                }
            }
        };
    }

    public static <T> Backpressure<T> dropLatest(int capacity) {
        return new Backpressure<T>() {
            @Override
            void apply(LinkedList<T> queue) {
                while (queue.size() > capacity) {
                    queue.removeLast();
                }
            }
        };
    }

    public static <T> Backpressure<T> dropOldest(int capacity) {
        return new Backpressure<T>() {
            @Override
            void apply(LinkedList<T> queue) {
                while (queue.size() > capacity) {
                    queue.removeFirst();
                }
            }
        };
    }

    public static class FullBufferException extends RuntimeException {
    }
}
