package io.xflow.cancellable;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;

import io.xflow.func.Cancellable;

/**
 * @author 7hens
 */
public class CompositeCancellable implements Cancellable {
    private AtomicBoolean cancelFlag = new AtomicBoolean(false);
    private Set<Cancellable> cancelableSet = new CopyOnWriteArraySet<>();

    @Override
    public void cancel() {
        if (cancelFlag.compareAndSet(false, true)) {
            for (Cancellable disposable : cancelableSet) {
                disposable.cancel();
            }
            cancelableSet.clear();
            onCancel();
        }
    }

    protected void onCancel() {
    }

    public CompositeCancellable add(Cancellable cancellable) {
        if (cancellable != this) {
            if (isCancelled()) {
                cancellable.cancel();
            } else {
                cancelableSet.add(cancellable);
            }
        }
        return this;
    }

    public boolean isCancelled() {
        return cancelFlag.get();
    }
}
