package cn.thens.xflow.flow;

/**
 * @author 7hens
 */
public class FlowFrom<T> extends AbstractFlow<T> {
    private final Iterable<T> iterable;

    FlowFrom(Iterable<T> iterable) {
        this.iterable = iterable;
    }

    @Override
    protected void onStart(CollectorEmitter<T> emitter) {
        for (T item : iterable) {
            emitter.data(item);
        }
        emitter.complete();
    }
}
