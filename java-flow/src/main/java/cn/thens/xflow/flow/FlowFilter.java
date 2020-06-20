package cn.thens.xflow.flow;

import java.util.HashSet;
import java.util.Set;

import cn.thens.xflow.func.Func1;
import cn.thens.xflow.func.Predicate;


/**
 * @author 7hens
 */
class FlowFilter<T> implements Flow.Operator<T, T> {
    private final Predicate<T> predicate;

    FlowFilter(Predicate<T> predicate) {
        this.predicate = predicate;
    }

    @Override
    public Collector<T> apply(final Emitter<T> emitter) {
        return new Collector<T>() {
            @Override
            public void onCollect(Reply<T> reply) {
                if (reply.isTerminated()) {
                    emitter.error(reply.error());
                } else {
                    try {
                        T data = reply.data();
                        if (predicate.test(data)) {
                            emitter.data(data);
                        }
                    } catch (Throwable e) {
                        emitter.error(e);
                    }
                }
            }
        };
    }

    static <T, K> FlowFilter<T> distinct(final Func1<T, K> keySelector) {
        return new FlowFilter<>(new Predicate<T>() {
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
        });
    }

    static <T> FlowFilter<T> distinct() {
        return distinct(FuncUtils.self());
    }

    static <T, K> FlowFilter<T> distinctUntilChanged(final Func1<T, K> keySelector) {
        return new FlowFilter<>(new Predicate<T>() {
            private K lastKey;

            @Override
            public boolean test(T data) throws Throwable {
                K key = keySelector.invoke(data);
                if (key.equals(lastKey)) {
                    return false;
                }
                lastKey = key;
                return true;
            }
        });
    }

    static <T> FlowFilter<T> distinctUntilChanged() {
        return distinctUntilChanged(FuncUtils.self());
    }
}
