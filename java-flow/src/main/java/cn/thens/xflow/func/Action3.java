package cn.thens.xflow.func;

/**
 * @author 7hens
 */
public interface Action3<P1, P2, P3> extends Action {
    void invoke(P1 p1, P2 p2, P3 p3) throws Throwable;
}
