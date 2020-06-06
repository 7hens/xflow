package io.rxflow.collector;

import io.rxflow.func.Consumer;
import io.rxflow.func.Functions;

/**
 * @author 7hens
 */
public final class RxCollectors {
    public static <T> RxCollector<T> create(Consumer<? super T> onCollect, Consumer<Throwable> onTerminate) {
        return new RxCollector<T>() {
            @Override
            public void onCollect(T t) throws Throwable {
                onCollect.accept(t);
            }

            @Override
            public void onTerminate(Throwable e) throws Throwable {
                onTerminate.accept(e);
            }
        };
    }

    public static <T> RxCollector<T> each(Consumer<? super T> onCollect) {
        return create(onCollect, Functions.emptyConsumer());
    }

    public static <T> RxCollector<T> empty() {
        return create(Functions.emptyConsumer(), Functions.emptyConsumer());
    }
}
