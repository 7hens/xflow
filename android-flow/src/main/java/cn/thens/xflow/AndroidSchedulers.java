package cn.thens.xflow;

import android.os.Looper;

import cn.thens.xflow.scheduler.Scheduler;

/**
 * @author 7hens
 */
@SuppressWarnings("WeakerAccess")
public final class AndroidSchedulers {
    public static Scheduler from(Looper looper) {
        return new LooperScheduler(looper);
    }

    private static final Scheduler MAIN_THREAD = from(Looper.getMainLooper());

    public static Scheduler mainThread() {
        return MAIN_THREAD;
    }

    public static Scheduler myThread() {
        return from(Looper.myLooper());
    }
}
