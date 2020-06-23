package cn.thens.rxflow;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.TimeUnit;

import cn.thens.xflow.AndroidFlow;
import cn.thens.xflow.flow.CollectorHelper;
import cn.thens.xflow.flow.Flow;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Flow.interval(1, TimeUnit.SECONDS)
//                .onCollect(new CollectorHelper<Long>() {
//                    @Override
//                    protected void onEach(Long data) throws Throwable {
//                        Log.d("@Flow", "test lifecycle: " + data);
//                    }
//
//                    @Override
//                    protected void onTerminate(Throwable error) throws Throwable {
//                        super.onTerminate(error);
//                        Log.d("@Flow", "test lifecycle: " + error.getClass().getName());
//                    }
//                })
                .autoCancel(AndroidFlow.lifecycle(this))
                .onCollect(new CollectorHelper<Long>() {
                    @Override
                    protected void onEach(Long data) throws Throwable {
                        Log.d("@Flow", "test lifecycle: " + data);
                    }

                    @Override
                    protected void onTerminate(Throwable error) throws Throwable {
                        super.onTerminate(error);
                        Log.d("@Flow", "test lifecycle: " + error.getClass().getName());
                    }
                })
                .collect();
    }
}
