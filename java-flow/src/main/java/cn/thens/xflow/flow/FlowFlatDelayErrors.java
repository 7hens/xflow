package cn.thens.xflow.flow;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 7hens
 */
public class FlowFlatDelayErrors<T> implements Flow.Operator<Flow<T>, Flow<T>> {

    @Override
    public Collector<Flow<T>> apply(final Emitter<Flow<T>> emitter) {
        return new Collector<Flow<T>>() {
            final List<Throwable> errors = new ArrayList<>();
            final AtomicInteger restFlowCount = new AtomicInteger(1);

            @Override
            public void onCollect(Reply<Flow<T>> reply) {
                if (reply.isTerminated()) {
                    Throwable error = reply.error();
                    if (error == null) {
                        onEachFlowTerminate();
                    } else {
                        emitter.error(error);
                    }
                    return;
                }
                restFlowCount.incrementAndGet();
                emitter.data(reply.data()
                        .onCollect(new CollectorHelper<T>() {
                            @Override
                            protected void onTerminate(Throwable error) throws Throwable {
                                super.onTerminate(error);
                                if (error != null) {
                                    errors.add(error);
                                }
                                onEachFlowTerminate();
                            }
                        })
                        .catchError(Flow.empty()));
            }

            private void onEachFlowTerminate() {
                if (restFlowCount.decrementAndGet() == 0) {
                    emitter.error(errors.isEmpty() ? null : new CompositeException(errors));
                }
            }
        };
    }
}
