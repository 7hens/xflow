package cn.thens.xflow.flow;


import cn.thens.xflow.func.Action1;
import cn.thens.xflow.func.Func0;

/**
 * @author 7hens
 */
final class SimpleFlows {
    static <T> Flow<T> create(Action1<Emitter<T>> onStart) {
        return new AbstractFlow<T>() {
            @Override
            protected void onStart(CollectorEmitter<T> emitter) throws Throwable {
                onStart.invoke(emitter);
            }
        };
    }

    static <T> Flow<T> empty() {
        return new AbstractFlow<T>() {
            @Override
            protected void onStart(CollectorEmitter<T> emitter) {
                emitter.complete();
            }
        };
    }

    static <T> Flow<T> never() {
        return new AbstractFlow<T>() {
            @Override
            protected void onStart(CollectorEmitter<T> emitter) {
            }
        };
    }

    static <T> Flow<T> error(final Throwable e) {
        return new AbstractFlow<T>() {
            @Override
            protected void onStart(CollectorEmitter<T> emitter) {
                emitter.error(e);
            }
        };
    }

    static <T> Flow<T> defer(final Func0<Flow<T>> flowFactory) {
        return new AbstractFlow<T>() {
            @Override
            protected void onStart(CollectorEmitter<T> emitter) throws Throwable {
                flowFactory.invoke().collect(emitter);
            }
        };
    }
}
