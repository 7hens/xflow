package cn.thens.xflow.func;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author 7hens
 */
@SuppressWarnings("WeakerAccess")
public class Lazy<T> {
    private AtomicBoolean shouldEnter = new AtomicBoolean(true);
    private AtomicBoolean shouldLeave = new AtomicBoolean(false);
    private final Supplier<T> supplier;
    private T result;

    private Lazy(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public T get() {
        if (isInitialized()) return result;
        if (shouldEnter.compareAndSet(true, false)) {
            try {
                result = supplier.get();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            } finally {
                shouldLeave.set(true);
            }
        }
        //noinspection StatementWithEmptyBody
        while (!shouldLeave.get()) ;
        return result;
    }

    public boolean isInitialized() {
        return shouldLeave.get();
    }

    public static <T> Lazy<T> of(Supplier<T> supplier) {
        return new Lazy<>(supplier);
    }
}
