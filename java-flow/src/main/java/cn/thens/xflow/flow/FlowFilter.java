package cn.thens.xflow.flow;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import cn.thens.xflow.func.Function;
import cn.thens.xflow.func.Functions;
import cn.thens.xflow.func.Predicate;
import cn.thens.xflow.func.PredicateHelper;


/**
 * @author 7hens
 */
abstract class FlowFilter<T> implements FlowOperator<T, T> {
    @Override
    public Collector<T> apply(final Emitter<? super T> emitter) {
        return new Collector<T>() {
            @Override
            public void onCollect(Reply<? extends T> reply) {
                if (reply.isTerminated()) {
                    onTerminated(emitter, reply.error());
                    return;
                }
                try {
                    T data = reply.data();
                    if (test(data)) {
                        emitter.data(data);
                    }
                } catch (Throwable e) {
                    emitter.error(e);
                }
            }
        };
    }

    protected abstract boolean test(T data) throws Throwable;

    void onTerminated(Emitter<? super T> emitter, Throwable error) {
        emitter.error(error);
    }

    static <T> FlowFilter<T> filter(Predicate<? super T> predicate) {
        return new FlowFilter<T>() {
            @Override
            protected boolean test(T data) throws Throwable {
                return predicate.test(data);
            }
        };
    }

    static <T, K> FlowFilter<T> distinct(final Function<? super T, ? extends K> keySelector) {
        return new FlowFilter<T>() {
            private Set<K> collectedKeys = new HashSet<>();

            @Override
            public boolean test(T data) throws Throwable {
                K key = keySelector.apply(data);
                if (collectedKeys.contains(key)) {
                    return false;
                }
                collectedKeys.add(key);
                return true;
            }

            @Override
            protected void onTerminated(Emitter<? super T> emitter, Throwable error) {
                super.onTerminated(emitter, error);
                collectedKeys.clear();
            }
        };
    }

    static <T> FlowFilter<T> distinct() {
        return distinct(Functions.self());
    }

    static <T, K> FlowFilter<T> distinctUntilChanged(final Function<? super T, ? extends K> keySelector) {
        return new FlowFilter<T>() {
            private K lastKey = null;

            @Override
            public boolean test(T data) throws Throwable {
                K key = keySelector.apply(data);
                if (key.equals(lastKey)) {
                    return false;
                }
                lastKey = key;
                return true;
            }

            @Override
            protected void onTerminated(Emitter<? super T> emitter, Throwable error) {
                super.onTerminated(emitter, error);
                lastKey = null;
            }
        };
    }

    static <T> FlowFilter<T> distinctUntilChanged() {
        return distinctUntilChanged(Functions.self());
    }

    static <T> FlowFilter<T> skip(int count) {
        return FlowFilter.filter(PredicateHelper.skip(count));
    }

    static <T> FlowFilter<T> last(Predicate<? super T> predicate) {
        return new FlowFilter<T>() {
            AtomicBoolean hasValue = new AtomicBoolean(false);
            T lastValue;

            @Override
            protected boolean test(T data) throws Throwable {
                if (predicate.test(data)) {
                    lastValue = data;
                    hasValue.set(true);
                }
                return false;
            }

            @Override
            void onTerminated(Emitter<? super T> emitter, Throwable error) {
                if (error == null) {
                    if (hasValue.get()) {
                        emitter.data(lastValue);
                        emitter.complete();
                    } else {
                        emitter.error(new NoSuchElementException());
                    }
                } else {
                    super.onTerminated(emitter, error);
                }
            }
        };
    }

    static <T> FlowFilter<T> last() {
        return last(PredicateHelper.alwaysTrue());
    }

    static <T> FlowFilter<T> ignoreElements() {
        return FlowFilter.filter(PredicateHelper.alwaysFalse());
    }
}
