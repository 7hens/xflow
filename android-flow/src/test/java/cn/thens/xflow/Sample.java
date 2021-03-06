package cn.thens.xflow;

import androidx.appcompat.app.AppCompatActivity;

import cn.thens.xflow.flow.CollectorHelper;
import cn.thens.xflow.flow.Flow;
import cn.thens.xflow.scheduler.Schedulers;

/**
 * @author 7hens
 */
public class Sample {
    private void readMe(AppCompatActivity activity) {
        Flow.just(1, 2, 3, 4, 5)
                .take(3)
                .mapToFlow(it -> Flow.just(it + 10, it + 20))
                .delayErrors()
                .flatMerge()
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
                .autoCancel(LifecycleFlow.from(activity))
                .collect();
    }
}
