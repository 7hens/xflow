package cn.thens.xflow.flow;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 7hens
 */
abstract class FlowFlatHelper {
    private final List<Throwable> errors = new ArrayList<>();
    private final AtomicInteger restFlowCount = new AtomicInteger(1);
    private final boolean delayError;

    FlowFlatHelper(boolean delayError) {
        this.delayError = delayError;
    }

    abstract void onTerminate(Throwable error);

    void onOuterCollect(Reply<?> reply) {
        if (reply.isTerminated()) {
            Throwable error = reply.error();
            if (error == null) {
                onEachFlowTerminate();
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
            if (error != null) {
                if (delayError) {
                    errors.add(error);
                } else {
                    onTerminate(error);
                }
            }
            onEachFlowTerminate();
        }
    }

    private void onEachFlowTerminate() {
        if (restFlowCount.decrementAndGet() == 0) {
            onTerminate(errors.isEmpty() ? null : new CompositeException(errors));
        }
    }

    static FlowFlatHelper create(boolean delayError, Emitter<?> emitter) {
        return new FlowFlatHelper(delayError) {
            @Override
            void onTerminate(Throwable error) {
                emitter.error(error);
            }
        };
    }
}
