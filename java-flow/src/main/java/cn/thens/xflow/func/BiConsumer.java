package cn.thens.xflow.func;

/**
 * @author 7hens
 */
public interface BiConsumer<P1, P2> {
    void accept(P1 p1, P2 p2) throws Throwable;
}
