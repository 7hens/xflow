package cn.thens.xflow.cancellable;

/**
 * @author 7hens
 */
public interface Cancellable {
    void cancel();

    boolean isCancelled();
}
