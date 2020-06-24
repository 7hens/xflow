package cn.thens.xflow.cancellable;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author 7hens
 */
public class CompositeCancellable implements Cancellable {
    private AtomicBoolean disposeFlag = new AtomicBoolean(false);
    private Set<Cancellable> cancelableSet = new CopyOnWriteArraySet<>();

    @Override
    public void cancel() {
        if (disposeFlag.compareAndSet(false, true)) {
            onCancel();
            for (Cancellable disposable : cancelableSet) {
                if (!disposable.isCancelled()) {
                    disposable.cancel();
                }
            }
            cancelableSet.clear();
        }
    }

    @Override
    public boolean isCancelled() {
        return disposeFlag.get();
    }

    protected void onCancel() {
    }

    public void addCancellable(Cancellable disposable) {
        if (disposable != this) {
            if (isCancelled()) {
                disposable.cancel();
            } else {
                cancelableSet.add(disposable);
            }
        }
    }

    private static CompositeCancellable CANCELLED = new CompositeCancellable();

    static {
        CANCELLED.cancel();
    }

    public static CompositeCancellable cancelled() {
        return CANCELLED;
    }
}
