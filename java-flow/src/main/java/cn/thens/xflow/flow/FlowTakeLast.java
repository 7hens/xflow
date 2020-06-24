package cn.thens.xflow.flow;


import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * @author 7hens
 */
class FlowTakeLast<T> implements Flow.Operator<T, T> {
    private final List<T> cacheItems = new CopyOnWriteArrayList<>();
    private final int count;

    FlowTakeLast(int count) {
        this.count = count;
    }

    @Override
    public Collector<T> apply(Emitter<T> emitter) {
        return reply -> {
            if (reply.isTerminated()) {
                Throwable error = reply.error();
                if (error == null) {
                    for (T item : cacheItems) {
                        emitter.data(item);
                    }
                }
                emitter.emit(reply);
                return;
            }
            if (count <= 0) return;
            while (cacheItems.size() == count) {
                cacheItems.remove(0);
            }
            cacheItems.add(reply.data());
        };
    }
}
