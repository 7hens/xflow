package cn.thens.xflow.func;

/**
 * @author 7hens
 */
public interface Action4<P1, P2, P3, P4> extends Action {
    void invoke(P1 p1, P2 p2, P3 p3, P4 p4);
}
