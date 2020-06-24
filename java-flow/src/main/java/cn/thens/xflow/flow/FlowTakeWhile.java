package cn.thens.xflow.flow;


import cn.thens.xflow.func.Predicate;

/**
 * @author 7hens
 */
abstract class FlowTakeWhile<T> implements Flow.Operator<T, T> {
    @Override
    public Collector<T> apply(Emitter<T> emitter) {
        return new Collector<T>() {
            @Override
            public void onCollect(Reply<T> reply) {
                if (reply.isTerminated()) {
                    emitter.error(reply.error());
                    return;
                }
                try {
                    if (test(reply.data())) {
                        emitter.emit(reply);
                    } else {
                        emitter.complete();
                    }
                } catch (Throwable e) {
                    emitter.error(e);
                }
            }
        };
    }

    protected abstract boolean test(T data) throws Throwable;

    static <T> FlowTakeWhile<T> takeWhile(Predicate<T> predicate) {
        return new FlowTakeWhile<T>() {
            @Override
            protected boolean test(T data) throws Throwable {
                return predicate.test(data);
            }
        };
    }

    static <T> FlowTakeWhile<T> takeUntil(T t) {
        return new FlowTakeWhile<T>() {
            @Override
            protected boolean test(T data) throws Throwable {
                return data != t;
            }
        };
    }
}
