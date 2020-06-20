package cn.thens.xflow.flow;

/**
 * @author 7hens
 */
@SuppressWarnings("unchecked")
public final class FlowOperators {
    private static Flow.Operator FLAT_MERGE = new FlowFlatMerge();

    public static <T> Flow.Operator<Flow<T>, T> flatMerge() {
        return FLAT_MERGE;
    }

    private static Flow.Operator FLAT_CONCAT = new FlowFlatConcat();

    public static <T> Flow.Operator<Flow<T>, T> flatConcat() {
        return FLAT_CONCAT;
    }

    private static Flow.Operator FLAT_SWITCH = new FlowFlatSwitch();

    public static <T> Flow.Operator<Flow<T>, T> flatSwitch() {
        return FLAT_SWITCH;
    }
}
