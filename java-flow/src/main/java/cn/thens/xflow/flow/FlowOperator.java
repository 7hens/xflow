package cn.thens.xflow.flow;

public interface FlowOperator<Up, Dn> {
    Collector<Up> apply(Emitter<Dn> emitter);
}
