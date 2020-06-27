package cn.thens.xflow.flow;

import java.util.List;

import cn.thens.xflow.func.Func1;

public abstract class PolyFlow<T> extends Flow<Flowable<T>> {
    public <R> R polyTo(Func1<? super PolyFlow<T>, ? extends R> converter) {
        try {
            return converter.invoke(this);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public PolyFlow<T> delayErrors() {
        return new PolyFlowDelayErrors<>(this);
    }

    public Flow<T> flatConcat() {
        return new PolyFlowFlatConcat<>(this);
    }

    public Flow<T> flatMerge() {
        return new PolyFlowFlatMerge<>(this);
    }

    public Flow<T> flatSwitch() {
        return new PolyFlowFlatSwitch<>(this);
    }

    public Flow<List<T>> flatZip() {
        return new PolyFlowFlatZip<>(this);
    }

    public Flow<List<T>> flatToList() {
        return flatMap(it -> it.asFlow().toList());
    }
}
