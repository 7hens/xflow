package cn.thens.xflow.func;

/**
 * @author 7hens
 */
public interface Action1<P1> extends Action {
    void invoke(P1 p1)throws Throwable;
}
