package cn.thens.xflow.flow;


import cn.thens.xflow.func.Func0;
import cn.thens.xflow.func.Func1;

/**
 * @author 7hens
 */
class FlowDelay<T, R> extends AbstractFlow<R> {
    private final Flow<T> upFlow;
    private final Func0<? extends Flow> startDelay;
    private final Func1<? super T, ? extends Flow<R>> replyDelay;

    private FlowDelay(Flow<T> upFlow, Func0<? extends Flow<?>> startDelay, Func1<? super T, ? extends Flow<R>> replyDelay) {
        this.upFlow = upFlow;
        this.startDelay = startDelay;
        this.replyDelay = replyDelay;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onStart(CollectorEmitter<R> emitter) {
        try {
            startDelay.invoke().collect(emitter, new CollectorHelper() {
                @Override
                protected void onTerminate(Throwable error) throws Throwable {
                    super.onTerminate(error);
                    upFlow.collect(emitter, new CollectorHelper<T>() {
                        @Override
                        protected void onEach(T data) throws Throwable {
                            super.onEach(data);
                            replyDelay.invoke(data).collect(emitter, CollectorHelper.from(emitter));
                        }
                    });
                }
            });
        } catch (Throwable e) {
            emitter.error(e);
        }
    }
}
