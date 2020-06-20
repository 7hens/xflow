package cn.thens.xflow.func;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 7hens
 */
public final class Predicates {
    private static final Predicate ALWAYS = new Predicate() {
        @Override
        public boolean test(Object o) {
            return true;
        }
    };

    private static final Predicate NEVER = new Predicate() {
        @Override
        public boolean test(Object o) {
            return false;
        }
    };

    @SuppressWarnings("unchecked")
    public static <T> Predicate<T> always() {
        return ALWAYS;
    }

    @SuppressWarnings("unchecked")
    public static <T> Predicate<T> never() {
        return NEVER;
    }

    public static <T> Predicate<T> limit(int count) {
        final AtomicInteger restCount = new AtomicInteger(count);
        return new Predicate<T>() {
            @Override
            public boolean test(T t) {
                return restCount.decrementAndGet() >= 0;
            }
        };
    }
}
