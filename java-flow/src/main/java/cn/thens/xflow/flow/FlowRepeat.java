package cn.thens.xflow.flow;

import java.util.concurrent.atomic.AtomicInteger;

import cn.thens.xflow.func.Func0;
import cn.thens.xflow.func.Predicate;

/**
 * @author 7hens
 */
abstract class FlowRepeat<T> extends AbstractFlow<T> {
    private final Flow<T> upFlow;

    FlowRepeat(Flow<T> upFlow) {
        this.upFlow = upFlow;
    }

    @Override
    protected void onStart(CollectorEmitter<T> emitter) throws Throwable {
        upFlow.collect(emitter, new CollectorHelper<T>() {
            @Override
            protected void onEach(T data) throws Throwable {
                super.onEach(data);
                emitter.data(data);
            }

            @Override
            protected void onTerminate(Throwable error) throws Throwable {
                super.onTerminate(error);
                try {
                    onFlowTerminate(emitter);
                } catch (Throwable e){
                    emitter.error(error);
                }
            }
        });
    }

    abstract void onFlowTerminate(CollectorEmitter<T> emitter) throws Throwable;

    static <T> FlowRepeat<T> repeat(Flow<T> upFlow) {
        return new FlowRepeat<T>(upFlow) {
            @Override
            void onFlowTerminate(CollectorEmitter<T> emitter) throws Throwable {
                collect(emitter);
            }
        };
    }

    static <T> FlowRepeat<T> repeat(Flow<T> upFlow, Func0<Boolean> shouldRepeat) {
        return new FlowRepeat<T>(upFlow) {
            @Override
            void onFlowTerminate(CollectorEmitter<T> emitter) throws Throwable {
                if (shouldRepeat.invoke()) {
                    collect(emitter);
                }
            }
        };
    }

    static <T> FlowRepeat<T > repeat(Flow<T> upFlow, int count) {
        return new FlowRepeat<T>(upFlow) {
            AtomicInteger resetCount = new AtomicInteger(count);

            @Override
            void onFlowTerminate(CollectorEmitter<T> emitter) throws Throwable {
                if (resetCount.decrementAndGet() >= 0) {
                    collect(emitter);
                }
            }
        };
    }
}
