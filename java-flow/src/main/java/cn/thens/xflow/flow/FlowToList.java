package cn.thens.xflow.flow;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 7hens
 */
class FlowToList<T> implements Flow.Operator<T, List<T>> {
    @Override
    public Collector<T> apply(final Emitter<List<T>> emitter) {
        return new Collector<T>() {
            List<T> list = new ArrayList<>();

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
