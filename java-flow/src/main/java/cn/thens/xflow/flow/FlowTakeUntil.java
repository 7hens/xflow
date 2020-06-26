package cn.thens.xflow.flow;


import cn.thens.xflow.func.Predicate;
import cn.thens.xflow.func.PredicateHelper;

/**
 * @author 7hens
 */
abstract class FlowTakeUntil<T> implements FlowOperator<T, T> {
    @Override
    public Collector<T> apply(Emitter<T> emitter) {
        return new Collector<T>() {
            @Override
            public void onCollect(Reply<T> reply) {
                if (reply.isTerminated()) {
                    emitter.emit(reply);
                    return;
                }
                try {
                    emitter.emit(reply);
                    if (test(reply.data())) {
                        emitter.complete();
                    }
                } catch (Throwable e) {
                    emitter.error(e);
                }
            }
        };
    }

    protected abstract boolean test(T data) throws Throwable;

    static <T> FlowTakeUntil<T> takeUntil(Predicate<T> predicate) {
        return new FlowTakeUntil<T>() {
            @Override
            protected boolean test(T data) throws Throwable {
                return predicate.test(data);
            }
        };
    }

    static <T> FlowTakeUntil<T> takeUntil(T t) {
        return new FlowTakeUntil<T>() {
            @Override
            protected boolean test(T data) throws Throwable {
                return data == t;
            }
        };
    }

    static <T> FlowOperator<T, T> take(int count) {
        if (count <= 0) {
            return emitter -> {
                emitter.complete();
                return CollectorHelper.from(emitter);
            };
        }
        return takeUntil(PredicateHelper.skip(count - 1));
    }
}
