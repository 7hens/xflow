package cn.thens.xflow.flow;

/**
 * @author 7hens
 */
public interface Collector<T> {
    void onCollect(Reply<? extends T> reply);
}
