package cn.thens.xflow.flow;

/**
 * @author 7hens
 */
public class FlowRange extends AbstractFlow<Integer> {
    private final int start;
    private final int end;
    private final int step;

    FlowRange(int start, int end, int step) {
        if (step == 0) {
            throw new IllegalArgumentException("Step cannot be zero");
        }
        if ((end - start) * step < 0) {
            throw new IllegalArgumentException("Illegal range " + start + "~" + end + ", " + step);
        }
        this.start = start;
        this.end = end;
        this.step = step;
    }

    @Override
    protected void onStart(CollectorEmitter<Integer> emitter) {
        if (end > start) {
            for (int i = start; i <= end; i += step) {
                emitter.data(i);
            }
        } else {
            for (int i = start; i >= end; i += step) {
                emitter.data(i);
            }
        }
        emitter.complete();
    }

}
