package cn.thens.rxflow;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;

import java.util.concurrent.TimeUnit;

import cn.thens.xflow.LifecycleFlow;
import cn.thens.xflow.flow.Collector;
import cn.thens.xflow.flow.CollectorHelper;
import cn.thens.xflow.flow.Flow;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("@Flow", "start");
        Flow.interval(1, TimeUnit.SECONDS)
                .onCollect(newCollector("A"))
                .autoCancel(LifecycleFlow.from(this).takeUntil(Lifecycle.Event.ON_PAUSE))
                .onCollect(newCollector("B"))
                .collect();
    }

    private <T> Collector<T> newCollector(String tag) {
        return new CollectorHelper<T>() {
            @Override
            protected void onEach(T data) throws Throwable {
                Log.d("@Flow." + tag, "" + data);
            }

            @Override
            protected void onTerminate(Throwable error) throws Throwable {
                super.onTerminate(error);
                Log.d("@Flow." + tag, "" + error.getClass().getName());
            }
        };
    }
}
