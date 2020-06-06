package io.rxflow.flow;

import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;

import io.rxflow.cancellable.CompositeCancellable;
import io.rxflow.collector.RxCollector;
import io.rxflow.func.Cancellable;

/**
 * @author 7hens
 */
class Create<T> extends RxFlow<T> {
    private final Creator<T> creator;

    Create(Creator<T> creator) {
        this.creator = creator;
    }

    @Override
    public Cancellable collect(RxCollector<? super T> collector) {
        EmitterImpl<T> emitter = new EmitterImpl<>(collector);
        try {
            creator.create(emitter);
        } catch (Throwable e) {
            emitter.terminate(e);
        }
        return emitter;
    }

    private static class EmitterImpl<T> implements Emitter<T>, Cancellable {
        private final RxCollector<? super T> collector;
        private final AtomicBoolean isTerminated = new AtomicBoolean(false);

        private final CompositeCancellable cancellable = new CompositeCancellable() {
            @Override
            protected void onCancel() {
                super.onCancel();
                terminate(new CancellationException());
            }
        };

        EmitterImpl(RxCollector<? super T> collector) {
            this.collector = collector;
        }

        @Override
        public void emit(T t) {
            try {
                if (isTerminated()) return;
                collector.onCollect(t);
            } catch (Throwable e) {
                terminate(e);
            }
        }

        @Override
        public void terminate(Throwable e) {
            if (isTerminated.compareAndSet(false, true)) {
                try {
                    collector.onTerminate(e);
                } catch (Throwable error) {
                    throw new RuntimeException(e);
                }
            }
        }

        @Override
        public void cancel() throws Throwable {
            cancellable.cancel();
        }

        @Override
        public boolean isTerminated() {
            return isTerminated.get();
        }
    }
}
