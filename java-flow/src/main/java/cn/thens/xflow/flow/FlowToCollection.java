package cn.thens.xflow.flow;

import java.util.Collection;

/**
 * @author 7hens
 */
class FlowToCollection<T, C extends Collection<T>> implements Flow.Operator<T, C> {
    private final C list;

    FlowToCollection(C collection) {
        this.list = collection;
    }

    @Override
    public Collector<T> apply(final Emitter<C> emitter) {
        return new Collector<T>() {
            @Override
            public void onCollect(Reply<T> reply) {
                if (reply.isTerminated()) {
                    emitter.data(list);
                    emitter.error(reply.error());
                    return;
                }
                list.add(reply.data());
            }
        };
    }
}
