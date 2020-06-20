package cn.thens.xflow.flow;


import cn.thens.xflow.func.Predicate;

/**
 * @author 7hens
 */
class FlowTakeWhile<T> implements Flow.Operator<T, T> {
    private final Predicate<T> predicate;

    FlowTakeWhile(Predicate<T> predicate) {
        this.predicate = predicate;
    }

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
                    if (predicate.test(reply.data())) {
                        emitter.emit(reply);
                    } else {
                        emitter.complete();
                    }
                } catch (Throwable e){
                    emitter.error(e);
                }
            }
        };
    }
}
