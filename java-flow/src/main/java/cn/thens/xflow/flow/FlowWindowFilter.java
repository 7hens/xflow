package cn.thens.xflow.flow;

import java.util.concurrent.atomic.AtomicInteger;

import cn.thens.xflow.func.Predicate;

/**
 * @author 7hens
 */
abstract class FlowWindowFilter<T> extends AbstractFlow<Flow<T>> {
    private final Flow<T> upFlow;
    private Emitter<T> currentEmitter;

    private FlowWindowFilter(Flow<T> upFlow) {
        this.upFlow = upFlow;
    }

    abstract boolean shouldClose(T data) throws Throwable;

    @Override
    protected void onStart(CollectorEmitter<Flow<T>> emitter) throws Throwable {
        emitNewFlow(emitter);
        upFlow.collect(emitter, reply -> {
            if (reply.isTerminated()) {
                Throwable error = reply.error();
                if (currentEmitter != null) {
                    currentEmitter.error(error);
                } else {
                    emitter.data(Flow.error(error));
                }
                return;
            }
            try {
                emitInner(reply);
                if (shouldClose(reply.data())) {
                    emitNewFlow(emitter);
                }
            } catch (Throwable e) {
                emitInner(Reply.error(e));
            }
        });
    }

    private void emitNewFlow(CollectorEmitter<Flow<T>> emitter) {
        emitInner(Reply.complete());
        emitter.data(new AbstractFlow<T>() {
            @Override
            protected void onStart(CollectorEmitter<T> innerEmitter) throws Throwable {
                currentEmitter = innerEmitter;
            }
        });
    }

    private void emitInner(Reply<T> reply) {
        if (currentEmitter != null) {
            currentEmitter.emit(reply);
        }
    }

    static <T> FlowWindowFilter<T> window(Flow<T> upFlow, Predicate<T> shouldClose) {
        return new FlowWindowFilter<T>(upFlow) {
            @Override
            boolean shouldClose(T data) throws Throwable {
                return shouldClose.test(data);
            }
        };
    }

    static <T> FlowWindowFilter<T> window(Flow<T> upFlow, final int count) {
        return new FlowWindowFilter<T>(upFlow) {
            AtomicInteger restCount = new AtomicInteger(count);

            @Override
            boolean shouldClose(T data) throws Throwable {
                if (restCount.decrementAndGet() == 0) {
                    restCount.set(count);
                    return true;
                }
                return false;
            }
        };
    }
}
