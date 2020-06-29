package cn.thens.xflow;

import android.os.Build;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

import cn.thens.xflow.cancellable.CompositeCancellable;
import cn.thens.xflow.flow.Flow;

/**
 * @author 7hens
 */
public class LifecycleFlow {
    public static Flow<Lifecycle.Event> from(final LifecycleOwner lifecycleOwner) {
        return Flow.create(emitter -> {
            LifecycleEventObserver observer = new LifecycleEventObserver() {
                @Override
                public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
                    emitter.data(event);
                    if (event == Lifecycle.Event.ON_DESTROY) {
                        emitter.complete();
                    }
                }
            };
            lifecycleOwner.getLifecycle().addObserver(observer);
            emitter.addCancellable(new CompositeCancellable() {
                @Override
                protected void onCancel() {
                    super.onCancel();
                    lifecycleOwner.getLifecycle().removeObserver(observer);
                }
            });
        });
    }

    public static Flow<Lifecycle.Event> mock(final View view) {
        return Flow.create(emitter -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                if (view.isAttachedToWindow()) {
                    emitter.data(Lifecycle.Event.ON_CREATE);
                }
            }
            View.OnAttachStateChangeListener listener = new View.OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(View v) {
                    emitter.data(Lifecycle.Event.ON_CREATE);
                }

                @Override
                public void onViewDetachedFromWindow(View v) {
                    emitter.data(Lifecycle.Event.ON_DESTROY);
                    emitter.complete();
                    view.removeOnAttachStateChangeListener(this);
                }
            };
            view.addOnAttachStateChangeListener(listener);
            emitter.addCancellable(new CompositeCancellable() {
                @Override
                protected void onCancel() {
                    super.onCancel();
                    view.removeOnAttachStateChangeListener(listener);
                }
            });
        });
    }
}
