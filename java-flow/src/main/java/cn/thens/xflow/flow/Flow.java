package cn.thens.xflow.flow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
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
public abstract class Flow<T> implements Flowable<T> {
    @Override
    public Flow<T> asFlow() {
        return this;
    }

    protected abstract Cancellable collect(Scheduler scheduler, Collector<? super T> collector);

    public Cancellable collect() {
        return collect(Schedulers.core(), CollectorHelper.get());
    }

    Cancellable collect(Emitter<?> emitter, Collector<? super T> collector) {
        Cancellable cancellable = collect(emitter.scheduler(), collector);
        emitter.addCancellable(cancellable);
        return cancellable;
    }

    Cancellable collect(Emitter<? super T> emitter) {
        return collect(emitter, CollectorHelper.from(emitter));
    }

    public Flow<T> flowOn(Scheduler upScheduler) {
        return new FlowFlowOn<>(this, upScheduler);
    }

    public <R> R to(Func1<? super Flow<T>, ? extends  R> converter) {
        try {
            return converter.invoke(this);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public <R> Flow<R> transform(FlowOperator<T, R> operator) {
        return new FlowTransform<>(this, operator);
    }

    public Flow<T> onCollect(Collector<T> collector) {
        return new FlowOnCollect<>(this, collector);
    }

    @SafeVarargs
    public final PolyFlow<T> polyWith(Flow<T>... flows) {
        ArrayList<Flow<T>> flowList = new ArrayList<>();
        flowList.add(this);
        flowList.addAll(Arrays.asList(flows));
        return from(flowList).to(FlowX.poly());
    }

    public <R> Flow<R> map(Func1<? super T, ? extends R> mapper) {
        return transform(new FlowMap<>(mapper));
    }

    @Deprecated
    public <R> PolyFlow<R> polyMap(Func1<? super T, ? extends Flowable<R>> mapper) {
        return mapToFlow(mapper);
    }

    public <R> PolyFlow<R> mapToFlow(Func1<? super T, ? extends Flowable<R>> mapper) {
        return map(mapper).to(FlowX.poly());
    }

    public <R> Flow<R> flatMap(Func1<? super T, ? extends Flowable<R>> mapper) {
        return mapToFlow(mapper).flatMerge();
    }

    public <C extends Collection<T>> Flow<C> toCollection(C collection) {
        return transform(new FlowToCollection<>(collection));
    }

    public Flow<List<T>> toList() {
        return toCollection(new ArrayList<>());
    }

    public Flow<T> filter(Predicate<T> predicate) {
        return transform(FlowFilter.filter(predicate));
    }

    public Flow<T> ignoreElements() {
        return transform(FlowFilter.ignoreElements());
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

    public Flow<T> throttleFirst(Func1<T, Flow<?>> flowFactory) {
        return transform(FlowThrottleFirst.throttleFirst(flowFactory));
    }

    public Flow<T> throttleFirst(Flow<?> flow) {
        return transform(FlowThrottleFirst.throttleFirst(flow));
    }

    public Flow<T> throttleLast(Func1<T, Flow<?>> flowFactory) {
        return transform(FlowThrottleLast.throttleLast(flowFactory));
    }

    public Flow<T> throttleLast(Flow<?> flow) {
        return transform(FlowThrottleLast.throttleLast(flow));
    }

    public Flow<T> take(int count) {
        return transform(FlowTakeUntil.take(count));
    }

    public Flow<T> takeLast(int count) {
        return transform(new FlowTakeLast<>(count));
    }

    public Flow<T> takeWhile(Predicate<T> predicate) {
        return transform(FlowTakeWhile.takeWhile(predicate));
    }

    public Flow<T> takeUntil(Predicate<T> predicate) {
        return transform(FlowTakeUntil.takeUntil(predicate));
    }

    public Flow<T> takeUntil(T data) {
        return transform(FlowTakeUntil.takeUntil(data));
    }

    public Flow<T> first() {
        return transform(FlowElementAt.first());
    }

    public Flow<T> first(Predicate<T> predicate) {
        return transform(FlowElementAt.first(predicate));
    }

    public Flow<T> elementAt(int index) {
        return transform(FlowElementAt.elementAt(index));
    }

    public Flow<T> last() {
        return transform(FlowFilter.last());
    }

    public Flow<T> last(Predicate<T> predicate) {
        return transform(FlowFilter.last(predicate));
    }

    public Flow<T> repeat() {
        return FlowRepeat.repeat(this);
    }

    public Flow<T> repeat(int count) {
        return FlowRepeat.repeat(this, count);
    }

    public Flow<T> repeat(Func0<Boolean> shouldRepeat) {
        return FlowRepeat.repeat(this, shouldRepeat);
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

    public Flow<T> delay(Func1<? super Reply<? extends T>, ? extends Flow<?>> delayFunc) {
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

    public PolyFlow<T> window(Func0<Flow<?>> windowFlowFactory) {
        return FlowWindow.window(this, windowFlowFactory);
    }

    public PolyFlow<T> window(Flow<?> windowFlow) {
        return FlowWindow.window(this, windowFlow);
    }

    public PolyFlow<T> window(Predicate<T> shouldClose) {
        return FlowWindowFilter.window(this, shouldClose);
    }

    public PolyFlow<T> window(int count) {
        return FlowWindowFilter.window(this, count);
    }

    public Flow<List<T>> buffer(Flow<?> windowFlow) {
        return window(windowFlow).flatToList();
    }

    public Flow<List<T>> buffer(int count) {
        return window(count).flatToList();
    }

    public Flow<T> onBackpressure(Backpressure<T> backpressure) {
        return new FlowOnBackpressure<>(this, backpressure);
    }

    public static <T> Flow<T> create(Action1<? super Emitter<? super T>> onStart) {
        return FlowCreate.create(onStart);
    }

    public static <T> Flow<T> defer(Func0<Flow<T>> flowFactory) {
        return FlowCreate.defer(flowFactory);
    }

    @SafeVarargs
    public static <T> Flow<T> just(T... items) {
        return FlowCreate.fromArray(items);
    }

    public static <T> Flow<T> from(Iterable<T> iterable) {
        return FlowCreate.fromIterable(iterable);
    }

    public static <T> Flow<T> from(T[] array) {
        return FlowCreate.fromArray(array);
    }

    public static Flow<Integer> range(int start, int end, int step) {
        return FlowCreate.range(start, end, step);
    }

    public static Flow<Integer> range(int start, int end) {
        return range(start, end, end > start ? 1 : -1);
    }

    public static <T> Flow<T> empty() {
        return FlowCreate.empty();
    }

    public static <T> Flow<T> error(Throwable e) {
        return FlowCreate.error(e);
    }

    public static <T> Flow<T> never() {
        return FlowCreate.never();
    }

    public static Flow<Long> timer(long delay, TimeUnit unit) {
        return FlowCreate.timer(delay, unit);
    }

    public static Flow<Long> interval(long initialDelay, long period, TimeUnit unit) {
        return FlowCreate.interval(initialDelay, period, unit);
    }

    public static Flow<Long> interval(long period, TimeUnit unit) {
        return interval(period, period, unit);
    }
}
