package cn.thens.xflow.flow;

import cn.thens.xflow.func.Function;

/**
 * @author 7hens
 */
class FlowMap<T, R> implements FlowOperator<T, R> {
    private final Function<? super T, ? extends R> mapper;

    FlowMap(Function<? super T, ? extends R> mapper) {
        this.mapper = mapper;
    }

    @Override
    public Collector<? super T> apply(final Emitter<? super R> emitter) {
        return new Collector<T>() {
            @Override
            public void onCollect(Reply<? extends T> reply) {
                if (reply.isTerminated()) {
                    emitter.error(reply.error());
                    return;
                }
                try {
                    emitter.data(mapper.apply(reply.data()));
                } catch (Throwable e) {
                    emitter.error(e);
                }
            }
        };
    }
}
