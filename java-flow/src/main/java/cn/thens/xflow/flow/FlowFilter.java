package cn.thens.xflow.flow;

import java.util.HashSet;
import java.util.Set;

import cn.thens.xflow.func.Func1;
import cn.thens.xflow.func.Funcs;
import cn.thens.xflow.func.Predicate;
import cn.thens.xflow.func.PredicateHelper;


/**
 * @author 7hens
 */
abstract class FlowFilter<T> implements Flow.Operator<T, T> {
    @Override
    public Collector<T> apply(final Emitter<T> emitter) {
        return new Collector<T>() {
            @Override
            public void onCollect(Reply<T> reply) {
                if (reply.isTerminated()) {
                    Throwable error = reply.error();
                    emitter.error(error);
                    onTerminated(error);
                } else {
                    try {
                        T data = reply.data();
                        if (test(data)) {
                            emitter.data(data);
                        }
                    } catch (Throwable e) {
                        emitter.error(e);
                    }
                }
            }
        };
    }

    protected abstract boolean test(T data) throws Throwable;

    protected abstract void onTerminated(Throwable e);

    static <T> FlowFilter<T> filter(Predicate<T> predicate) {
        return new FlowFilter<T>() {
            @Override
            protected boolean test(T data) throws Throwable {
                return predicate.test(data);
            }

            @Override
            protected void onTerminated(Throwable e) {
            }
        };
    }

    static <T, K> FlowFilter<T> distinct(final Func1<T, K> keySelector) {
        return new FlowFilter<T>() {
            private Set<K> collectedKeys = new HashSet<>();

            @Override
            public boolean test(T data) throws Throwable {
                K key = keySelector.invoke(data);
                if (collectedKeys.contains(key)) {
                    return false;
                }
                collectedKeys.add(key);
                return true;
            }

            @Override
            protected void onTerminated(Throwable e) {
                collectedKeys.clear();
            }
        };
    }

    static <T> FlowFilter<T> distinct() {
        return distinct(Funcs.self());
    }

    static <T, K> FlowFilter<T> distinctUntilChanged(final Func1<T, K> keySelector) {
        return new FlowFilter<T>() {
            private K lastKey = null;

            @Override
            public boolean test(T data) throws Throwable {
                K key = keySelector.invoke(data);
                if (key.equals(lastKey)) {
                    return false;
                }
                lastKey = key;
                return true;
            }

            @Override
            protected void onTerminated(Throwable e) {
                lastKey = null;
            }
        };
    }

    static <T> FlowFilter<T> distinctUntilChanged() {
        return distinctUntilChanged(Funcs.self());
    }

    static <T> FlowFilter<T> skip(int count) {
        return FlowFilter.filter(PredicateHelper.skip(count));
    }
}
