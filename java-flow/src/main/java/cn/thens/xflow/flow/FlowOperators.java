package cn.thens.xflow.flow;

/**
 * @author 7hens
 */
@SuppressWarnings("unchecked")
public final class FlowOperators {
    private static Flow.Operator FLAT_MERGE = new FlowFlatMerge(false);

    public static <T> Flow.Operator<Flow<T>, T> flatMerge() {
        return FLAT_MERGE;
    }

    private static Flow.Operator FLAT_MERGE_DELAY_ERROR = new FlowFlatMerge(true);

    public static <T> Flow.Operator<Flow<T>, T> flatMergeDelayError() {
        return FLAT_MERGE_DELAY_ERROR;
    }

    private static Flow.Operator FLAT_CONCAT = new FlowFlatConcat(false);

    public static <T> Flow.Operator<Flow<T>, T> flatConcat() {
        return FLAT_CONCAT;
    }

    private static Flow.Operator FLAT_CONCAT_DELAY_ERROR = new FlowFlatConcat(true);

    public static <T> Flow.Operator<Flow<T>, T> flatConcatDelayError() {
        return FLAT_CONCAT_DELAY_ERROR;
    }

    private static Flow.Operator FLAT_SWITCH = new FlowFlatSwitch(false);

    public static <T> Flow.Operator<Flow<T>, T> flatSwitch() {
        return FLAT_SWITCH;
    }

    private static Flow.Operator FLAT_SWITCH_DELAY_ERROR = new FlowFlatSwitch(true);

    public static <T> Flow.Operator<Flow<T>, T> flatSwitchDelayError() {
        return FLAT_SWITCH_DELAY_ERROR;
    }

    private static Flow.Operator FLAT_ZIP = new FlowFlatZip(false);

    public static <T> Flow.Operator<Flow<T>, T> flatZip() {
        return FLAT_ZIP;
    }

    private static Flow.Operator FLAT_ZIP_DELAY_ERROR = new FlowFlatZip(true);

    public static <T> Flow.Operator<Flow<T>, T> flatZipDelayError() {
        return FLAT_ZIP_DELAY_ERROR;
    }
}
