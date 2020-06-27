package cn.thens.xflow.flow;

public interface Flowable<T> {
    Flow<T> asFlow();
}
