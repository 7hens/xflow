package cn.thens.xflow.flow;


import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * @author 7hens
 */
abstract class FlowBuffer<T> implements FlowOperator<T, T> {
    private final List<T> buffer = new CopyOnWriteArrayList<>();

    @Override
    public Collector<T> apply(Emitter<? super T> emitter) {
        return reply -> {
            if (reply.isTerminated()) {
                if (reply.error() == null) {
                    onComplete(emitter, buffer);
                }
                buffer.clear();
                emitter.emit(reply);
                return;
            }
            buffer.add(reply.data());
            onBuffer(emitter, buffer);
        };
    }

    abstract void onBuffer(Emitter<? super T> emitter, List<T> buffer);

    abstract void onComplete(Emitter<? super T> emitter, List<T> buffer);

    static <T> FlowBuffer<T> takeLast(int count) {
        return new FlowBuffer<T>() {
            @Override
            void onBuffer(Emitter<? super T> emitter, List<T> buffer) {
                if (buffer.size() > count) {
                    buffer.remove(0);
                }
            }

            @Override
            void onComplete(Emitter<? super T> emitter, List<T> buffer) {
                if (count > 0) {
                    for (T data : buffer) {
                        emitter.data(data);
                    }
                }
            }
        };
    }

    static <T> FlowBuffer<T> lastElement(int number) {
        if (number <= 0) {
            throw new IllegalArgumentException("number should be greater than 0, " +
                    "but actual value is " + number);
        }
        return new FlowBuffer<T>() {
            @Override
            void onBuffer(Emitter<? super T> emitter, List<T> buffer) {
                if (buffer.size() > number) {
                    buffer.remove(0);
                }
            }

            @Override
            void onComplete(Emitter<? super T> emitter, List<T> buffer) {
                if (buffer.size() < number) {
                    emitter.error(new NoSuchElementException());
                    return;
                }
                emitter.data(buffer.get(0));
            }
        };
    }

    static <T> FlowBuffer<T> skipLast(int count) {
        return new FlowBuffer<T>() {
            @Override
            void onBuffer(Emitter<? super T> emitter, List<T> buffer) {
                if (buffer.size() > count) {
                    emitter.data(buffer.remove(0));
                }
            }

            @Override
            void onComplete(Emitter<? super T> emitter, List<T> buffer) {
            }
        };
    }
}
