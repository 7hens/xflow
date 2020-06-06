package io.rxflow.cancellable;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;

import io.rxflow.func.Cancellable;

/**
 * @author 7hens
 */
public class CompositeCancellable implements Cancellable {
    private AtomicBoolean cancelFlag = new AtomicBoolean(false);
    private Set<Cancellable> cancelableSet = new CopyOnWriteArraySet<>();

    @Override
    public void cancel() throws Throwable {
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

    public CompositeCancellable add(Cancellable disposable) {
        if (disposable != this) {
            if (isCancelled()) {
                try {
                    disposable.cancel();
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            } else {
                cancelableSet.add(disposable);
            }
        }
        return this;
    }

    public boolean isCancelled() {
        return cancelFlag.get();
    }
}
