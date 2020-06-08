package io.xflow.func;

/**
 * @author 7hens
 */
@SuppressWarnings("unchecked")
public final class Functions {
    private static EmptyConsumer EMPTY_CONSUMER = new EmptyConsumer();

    public static <T> Consumer<T> emptyConsumer() {
        return EMPTY_CONSUMER;
    }

    private static class EmptyConsumer<T> implements Consumer<T> {
        @Override
        public void accept(T t) throws Throwable {
        }
    }
}
