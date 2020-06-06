package io.rxflow.flow;

/**
 * @author 7hens
 */
class CreateJust<T> implements RxFlow.Creator<T> {
    private final T[] items;

    CreateJust(T... items) {
        this.items = items;
    }

    @Override
    public void create(RxFlow.Emitter<? super T> emitter) throws Throwable {
        for (T item : items) {
            emitter.emit(item);
        }
        emitter.terminate(null);
    }
}
