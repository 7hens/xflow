package cn.thens.xflow;

import androidx.appcompat.app.AppCompatActivity;

import cn.thens.xflow.flow.CollectorHelper;
import cn.thens.xflow.flow.Flow;
import cn.thens.xflow.flow.FlowX;
import cn.thens.xflow.scheduler.Schedulers;

/**
 * @author 7hens
 */
public class Sample {
    private void readMe(AppCompatActivity activity) {
        Flow.just(1, 2, 3, 4, 5)
                .take(3)
                .map(it -> Flow.just(it + 10, it + 20))
                .transform(FlowX.delayErrors())
                .transform(FlowX.flatMerge())
                .onCollect(new CollectorHelper<Integer>() {
                    @Override
                    protected void onEach(Integer s) {
                        System.out.println(s);
                    }

                    @Override
                    protected void onComplete() {
                    }
                })
                .flowOn(Schedulers.io())
                .autoCancel(AndroidFlow.lifecycle(activity))
                .collect();
    }
}
