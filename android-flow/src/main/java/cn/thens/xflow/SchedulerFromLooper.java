package cn.thens.xflow;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.TimeUnit;

import cn.thens.xflow.cancellable.Cancellable;
import cn.thens.xflow.cancellable.CompositeCancellable;
import cn.thens.xflow.scheduler.Scheduler;

/**
 * @author 7hens
 */
public class SchedulerFromLooper extends Scheduler {
    private final Handler handler;

    SchedulerFromLooper(Looper looper) {
        this.handler = new Handler(looper);
    }

    @Override
    public Cancellable schedule(final Runnable runnable, long delay, TimeUnit unit) {
        handler.postDelayed(runnable, unit.toMillis(delay));
        return new CompositeCancellable() {
            @Override
            protected void onCancel() {
                super.onCancel();
                handler.removeCallbacks(runnable);
            }
        };
    }
}
