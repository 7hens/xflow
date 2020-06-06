package io.rxflow.flow;

/**
 * @author 7hens
 */
class CreateOf<T> implements RxFlow.Creator<T> {
    private final Iterable<? extends T> iterable;

    CreateOf(Iterable<? extends T> iterable) {
        this.iterable = iterable;
    }

    @Override
    public void create(RxFlow.Emitter<? super T> emitter) throws Throwable {
        for (T t : iterable) {
            emitter.emit(t);
        }
        emitter.terminate(null);
    }
}
