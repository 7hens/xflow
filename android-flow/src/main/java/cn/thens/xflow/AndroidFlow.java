package cn.thens.xflow;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

import cn.thens.xflow.flow.Flow;

/**
 * @author 7hens
 */
public class AndroidFlow {
    public static Flow<Lifecycle.Event> lifecycle(final LifecycleOwner lifecycleOwner) {
        return Flow.create(emitter -> {
            lifecycleOwner.getLifecycle().addObserver(new LifecycleEventObserver() {
                @Override
                public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
                    emitter.data(event);
                    if (event == Lifecycle.Event.ON_DESTROY) {
                        emitter.complete();
                    }
                }
            });
        });
    }

    public static Flow<Lifecycle.Event> lifecycle(final View view) {
        return Flow.create(emitter -> {
            view.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
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
            });
        });
    }
}
