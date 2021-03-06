package cn.thens.xflow.flow;


import cn.thens.xflow.cancellable.Cancellable;
import cn.thens.xflow.scheduler.Scheduler;

/**
 * @author 7hens
 */
public interface Emitter<T> {
    void emit(Reply<? extends T> reply);

    void data(T data);

    void error(Throwable error);

    void cancel();

    void complete();

    boolean isTerminated();

    void addCancellable(Cancellable cancellable);

    Scheduler scheduler();
}
