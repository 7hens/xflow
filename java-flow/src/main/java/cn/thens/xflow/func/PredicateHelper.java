package cn.thens.xflow.func;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import cn.thens.xflow.flow.CollectorHelper;
import cn.thens.xflow.flow.Flow;

/**
 * @author 7hens
 */
public abstract class PredicateHelper<T> implements Predicate<T> {

    public final PredicateHelper<T> not() {
        PredicateHelper<T> self = this;
        return new PredicateHelper<T>() {
            @Override
            public boolean test(T t) throws Throwable {
                return !self.test(t);
            }
        };
    }

    public final PredicateHelper<T> and(final Predicate<T> other) {
        PredicateHelper<T> self = this;
        return new PredicateHelper<T>() {
            @Override
            public boolean test(T t) throws Throwable {
                return self.test(t) && other.test(t);
            }
        };
    }

    public final PredicateHelper<T> or(final Predicate<T> other) {
        PredicateHelper<T> self = this;
        return new PredicateHelper<T>() {
            @Override
            public boolean test(T t) throws Throwable {
                return self.test(t) || other.test(t);
            }
        };
    }

    public final PredicateHelper<T> xor(final Predicate<T> other) {
        PredicateHelper<T> self = this;
        return new PredicateHelper<T>() {
            @Override
            public boolean test(T t) throws Throwable {
                boolean selfTest = self.test(t);
                boolean otherTest = other.test(t);
                return selfTest && !otherTest || !selfTest && otherTest;
            }
        };
    }

    private static final PredicateHelper ALWAYS = new PredicateHelper() {
        @Override
        public boolean test(Object o) {
            return true;
        }
    };

    private static final PredicateHelper NEVER = new PredicateHelper() {
        @Override
        public boolean test(Object o) {
            return false;
        }
    };

    @SuppressWarnings("unchecked")
    public static <T> PredicateHelper<T> always() {
        return ALWAYS;
    }

    @SuppressWarnings("unchecked")
    public static <T> PredicateHelper<T> never() {
        return NEVER;
    }

    public static <T> PredicateHelper<T> take(int count) {
        final AtomicInteger restCount = new AtomicInteger(count);
        return new PredicateHelper<T>() {
            @Override
            public boolean test(T t) {
                return restCount.decrementAndGet() >= 0;
            }
        };
    }

    public static <T> PredicateHelper<T> skip(int count) {
        final AtomicInteger restCount = new AtomicInteger(count);
        return new PredicateHelper<T>() {
            @Override
            public boolean test(T t) {
                return restCount.decrementAndGet() < 0;
            }
        };
    }

    public static <T> PredicateHelper<T> wrap(final Predicate<T> predicate) {
        return new PredicateHelper<T>() {
            @Override
            public boolean test(T t) throws Throwable {
                return predicate.test(t);
            }
        };
    }

    public static <T> PredicateHelper<T> of(@NotNull AtomicBoolean value) {
        return new PredicateHelper<T>() {
            @Override
            public boolean test(T t) throws Throwable {
                return value.get();
            }
        };
    }

    public static <T> PredicateHelper<T> eq(@NotNull AtomicReference<T> dataRef) {
        return new PredicateHelper<T>() {
            @Override
            public boolean test(T t) throws Throwable {
                return PredicateHelper.equals(t, dataRef.get());
            }
        };
    }

    public static <T> PredicateHelper<T> eq(@Nullable T data) {
        return eq(new AtomicReference<>(data));
    }

    private static boolean equals(Object a, Object b) {
        //noinspection EqualsReplaceableByObjectsCall
        return a != null ? a.equals(b) : b == null;
    }

    @SuppressWarnings("unchecked")
    public static <T> PredicateHelper<T> window(@NotNull Flow<?> flow) {
        final AtomicBoolean hasNext = new AtomicBoolean(true);
        flow.onCollect(new CollectorHelper() {
            @Override
            protected void onTerminate(Throwable error) throws Throwable {
                super.onTerminate(error);
                hasNext.set(false);
            }
        }).collect();
        return of(hasNext);
    }
}
