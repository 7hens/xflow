package io.xflow.flow.caller;

import io.xflow.func.Action;
import io.xflow.func.Cancellable;

/**
 * @author 7hens
 */
public interface Emitter<T> extends Cancellable {
    void emit(T t, Action action);

    void over(Throwable e, Action action);
}
