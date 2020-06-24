package cn.thens.xflow.flow;

import org.jetbrains.annotations.ApiStatus;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import cn.thens.xflow.cancellable.Cancellable;
import cn.thens.xflow.func.Action1;
import cn.thens.xflow.func.Func0;
import cn.thens.xflow.func.Func1;
import cn.thens.xflow.func.Func2;
import cn.thens.xflow.func.Predicate;
import cn.thens.xflow.scheduler.Scheduler;
import cn.thens.xflow.scheduler.Schedulers;

/**
 * @author 7hens
 */
@SuppressWarnings({"unused", "WeakerAccess", "UnusedReturnValue"})
public abstract class Flow<T> {
    public interface Operator<Up, Dn> {
        Collector<Up> apply(Emitter<Dn> emitter);
    }

    protected abstract Cancellable collect(Scheduler scheduler, Collector<T> collector);

    Cancellable collect(Emitter<?> emitter, Collector<T> collector) {
        Cancellable cancellable = collect(emitter.scheduler(), collector);
        emitter.addCancellable(cancellable);
        return cancellable;
    }

    Cancellable collect(Emitter<T> emitter) {
        return collect(emitter, CollectorHelper.from(emitter));
    }

    @SuppressWarnings("UnusedReturnValue")
    public Cancellable collect() {
        return collect(Schedulers.core(), CollectorHelper.get());
    }

    public Flow<T> flowOn(Scheduler upScheduler) {
        return new FlowFlowOn<T>(this, upScheduler);
    }

    public <R> R to(Func1<Flow<T>, R> func) {
        try {
            return func.invoke(this);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public <R> Flow<R> transform(Operator<T, R> operator) {
        return new FlowTransform<>(this, operator);
    }

    public Flow<T> onCollect(Collector<T> collector) {
        return new FlowOnCollect<>(this, collector);
    }

    public <R> Flow<R> map(Func1<T, R> mapper) {
        return transform(new FlowMap<>(mapper));
    }

    public <R> Flow<R> flatMap(Func1<T, Flow<R>> mapper) {
        return map(mapper).transform(FlowX.flatMerge());
    }

    public Flow<T> filter(Predicate<T> predicate) {
        return transform(FlowFilter.filter(predicate));
    }

    public <K> Flow<T> distinct(Func1<T, K> keySelector) {
        return transform(FlowFilter.distinct(keySelector));
    }

    public Flow<T> distinct() {
        return transform(FlowFilter.distinct());
    }

    public <K> Flow<T> distinctUntilChanged(Func1<T, K> keySelector) {
        return transform(FlowFilter.distinctUntilChanged(keySelector));
    }

    public Flow<T> distinctUntilChanged() {
        return transform(FlowFilter.distinctUntilChanged());
    }

    public Flow<T> skip(int count) {
        return transform(FlowFilter.skip(count));
    }

    public Flow<T> take(int count) {
        return transform(new FlowTake<>(count));
    }

    public Flow<T> takeWhile(Predicate<T> predicate) {
        return transform(FlowTakeWhile.takeWhile(predicate));
    }

    public Flow<T> takeUntil(T data) {
        return transform(FlowTakeWhile.takeUntil(data));
    }

    public Flow<T> first() {
        return transform(FlowTakeFirst.first());
    }

    public Flow<T> firstOrDefault(final T defaultValue) {
        return transform(FlowTakeFirst.firstOrDefault(defaultValue));
    }

    public Flow<T> first(Predicate<T> predicate) {
        return transform(FlowTakeFirst.first(predicate));
    }

    public Flow<T> firstOrDefault(Predicate<T> predicate, T defaultValue) {
        return transform(FlowTakeFirst.firstOrDefault(predicate, defaultValue));
    }

    public <R> Flow<R> reduce(R initialValue, Func2<R, ? super T, R> accumulator) {
        return FlowReduce.reduce(this, initialValue, accumulator);
    }

    public Flow<T> reduce(Func2<T, ? super T, T> accumulator) {
        return FlowReduce.reduce(this, accumulator);
    }

    public Flow<T> timeout(long timeout, TimeUnit unit, Flow<T> fallback) {
        return transform(new FlowTimeout<>(timeout, unit, fallback));
    }

    public Flow<T> timeout(long timeout, TimeUnit unit) {
        return timeout(timeout, unit, Flow.error(new TimeoutException()));
    }

    public Flow<T> delay(Func1<? super Reply<T>, ? extends Flow<?>> delayFunc) {
        return FlowDelay.delay(this, delayFunc);
    }

    public Flow<T> delay(Flow<?> delayFlow) {
        return FlowDelay.delay(this, delayFlow);
    }

    public Flow<T> delayStart(Func0<? extends Flow<?>> delayFunc) {
        return FlowDelayStart.delayStart(this, delayFunc);
    }

    public Flow<T> delayStart(Flow<?> delayFlow) {
        return FlowDelayStart.delayStart(this, delayFlow);
    }

    @ApiStatus.Experimental
    public Flow<T> autoCancel(Flow<?> cancelFlow) {
        return new FlowAutoCancel<>(this, cancelFlow);
    }

    public Flow<T> catchError(Func1<Throwable, Flow<T>> resumeFunc) {
        return FlowCatch.catchError(this, resumeFunc);
    }

    public Flow<T> catchError(Flow<T> resumeFlow) {
        return FlowCatch.catchError(this, resumeFlow);
    }

    public Flow<T> retry() {
        return FlowCatch.retry(this);
    }

    public Flow<T> retry(int count) {
        return FlowCatch.retry(this, count);
    }

    public Flow<T> retry(Predicate<Throwable> predicate) {
        return FlowCatch.retry(this, predicate);
    }

    public static <T> Flow<T> create(Action1<Emitter<T>> onStart) {
        return SimpleFlows.create(onStart);
    }

    public static <T> Flow<T> defer(Func0<Flow<T>> flowFactory) {
        return SimpleFlows.defer(flowFactory);
    }

    @SafeVarargs
    public static <T> Flow<T> just(T... items) {
        return new FlowJust<>(items);
    }

    public static <T> Flow<T> from(Iterable<T> iterable) {
        return new FlowFrom<>(iterable);
    }

    public static <T> Flow<T> from(T[] array) {
        return new FlowJust<>(array);
    }

    public static Flow<Integer> range(int start, int end, int step) {
        return new FlowRange(start, end, step);
    }

    public static Flow<Integer> range(int start, int end) {
        return range(start, end, end > start ? 1 : -1);
    }

    public static <T> Flow<T> empty() {
        return SimpleFlows.empty();
    }

    public static <T> Flow<T> error(Throwable e) {
        return SimpleFlows.error(e);
    }

    public static <T> Flow<T> never() {
        return SimpleFlows.never();
    }

    public static Flow<Long> timer(long delay, TimeUnit unit) {
        return new FlowTimer(delay, unit);
    }

    public static Flow<Long> interval(long initialDelay, long period, TimeUnit unit) {
        return new FlowInterval(initialDelay, period, unit);
    }

    public static Flow<Long> interval(long period, TimeUnit unit) {
        return interval(period, period, unit);
    }
}
