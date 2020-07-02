package cn.thens.xflow.flow;


import cn.thens.xflow.func.Predicate;

/**
 * @author 7hens
 */
abstract class FlowTakeWhile<T> implements FlowOperator<T, T> {
    @Override
    public Collector<T> apply(Emitter<? super T> emitter) {
        return new Collector<T>() {
            @Override
            public void onCollect(Reply<? extends T> reply) {
                if (reply.isTerminal()) {
                    emitter.emit(reply);
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

    static <T> FlowTakeWhile<T> takeWhile(Predicate<? super T> predicate) {
        return new FlowTakeWhile<T>() {
            @Override
            protected boolean test(T data) throws Throwable {
                return predicate.test(data);
            }
        };
    }
}
