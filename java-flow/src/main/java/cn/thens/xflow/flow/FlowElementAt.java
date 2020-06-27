package cn.thens.xflow.flow;


import java.util.NoSuchElementException;

import cn.thens.xflow.func.Predicate;
import cn.thens.xflow.func.PredicateHelper;

/**
 * @author 7hens
 */
abstract class FlowElementAt<T> implements FlowOperator<T, T> {
    @Override
    public Collector<T> apply(Emitter<? super T> emitter) {
        return reply -> {
            if (reply.isTerminated()) {
                emitter.error(new NoSuchElementException());
                return;
            }
            try {
                if (test(reply.data())) {
                    emitter.emit(reply);
                    emitter.complete();
                }
            } catch (Throwable e) {
                emitter.error(e);
            }
        };
    }

    abstract boolean test(T data) throws Throwable;

    static <T> FlowElementAt<T> first(final Predicate<? super T> predicate) {
        return new FlowElementAt<T>() {
            @Override
            boolean test(T data) throws Throwable {
                return predicate.test(data);
            }
        };
    }

    static <T> FlowElementAt<T> first() {
        return first(PredicateHelper.alwaysTrue());
    }

    static <T> FlowElementAt<T> elementAt(int index) {
        return first(PredicateHelper.skip(index));
    }
}
