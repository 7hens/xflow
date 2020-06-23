package cn.thens.xflow.flow;

/**
 * @author 7hens
 */
@SuppressWarnings("unchecked")
public final class FlowX {
    private static Flow.Operator FLAT_DELAY_ERRORS = new FlowXDelayErrors();

    public static <T> Flow.Operator<Flow<T>, Flow<T>> delayErrors() {
        return FLAT_DELAY_ERRORS;
    }

    private static Flow.Operator FLAT_MERGE = new FlowXFlatMerge();

    public static <T> Flow.Operator<Flow<T>, T> flatMerge() {
        return FLAT_MERGE;
    }

    private static Flow.Operator FLAT_CONCAT = new FlowXFlatConcat();

    public static <T> Flow.Operator<Flow<T>, T> flatConcat() {
        return FLAT_CONCAT;
    }

    private static Flow.Operator FLAT_SWITCH = new FlowXFlatSwitch();

    public static <T> Flow.Operator<Flow<T>, T> flatSwitch() {
        return FLAT_SWITCH;
    }

    private static Flow.Operator FLAT_ZIP = new FlowXFlatZip();

    public static <T> Flow.Operator<Flow<T>, T> flatZip() {
        return FLAT_ZIP;
    }
}
