package cn.thens.xflow.flow;

/**
 * @author 7hens
 */
public class FlowJust<T> extends AbstractFlow<T> {
    private final T[] items;

    public FlowJust(T[] items) {
        this.items = items;
    }

    @Override
    protected void onStart(CollectorEmitter<T> emitter) {
        for (T item : items) {
            emitter.data(item);
        }
        emitter.complete();
    }
}
