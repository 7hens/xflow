package cn.thens.xflow.func;

/**
 * @author 7hens
 */
public interface Action2<P1, P2> extends Action {
    void invoke(P1 p1, P2 p2)throws Throwable;
}
