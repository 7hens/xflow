package cn.thens.xflow.flow;

import cn.thens.xflow.func.Func1;

/**
 * @author 7hens
 */
class FlowMap<T, R> implements Flow.Operator<T, R> {
    private final Func1<T, R> mapper;

    FlowMap(Func1<T, R> mapper) {
        this.mapper = mapper;
    }

    @Override
    public Collector<T> apply(final Emitter<R> emitter) {
        return new Collector<T>() {
            @Override
            public void onCollect(Reply<T> reply) {
                if (reply.isTerminated()) {
                    emitter.error(reply.error());
                } else {
                    try {
                        emitter.data(mapper.invoke(reply.data()));
                    } catch (Throwable e) {
                        emitter.error(e);
                    }
                }
            }
        };
    }
}
