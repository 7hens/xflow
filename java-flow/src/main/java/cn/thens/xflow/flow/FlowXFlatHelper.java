package cn.thens.xflow.flow;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 7hens
 */
abstract class FlowXFlatHelper {
    private final AtomicInteger restFlowCount = new AtomicInteger(1);

    abstract void onTerminate(Throwable error);

    void onOuterCollect(Reply<?> reply) {
        if (reply.isTerminated()) {
            Throwable error = reply.error();
            if (error == null) {
                onEachFlowComplete();
            } else {
                onTerminate(error);
            }
            return;
        }
        restFlowCount.incrementAndGet();
    }

    void onInnerCollect(Reply<?> reply) {
        if (reply.isTerminated()) {
            Throwable error = reply.error();
            if (error == null) {
                onEachFlowComplete();
            } else {
                onTerminate(error);
            }
        }
    }

    private void onEachFlowComplete() {
        if (restFlowCount.decrementAndGet() == 0) {
            onTerminate(null);
        }
    }

    static FlowXFlatHelper create(Emitter<?> emitter) {
        return new FlowXFlatHelper() {
            @Override
            void onTerminate(Throwable error) {
                emitter.error(error);
            }
        };
    }
}