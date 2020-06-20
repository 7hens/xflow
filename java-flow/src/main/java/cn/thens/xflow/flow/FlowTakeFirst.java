package cn.thens.xflow.flow;


import java.util.NoSuchElementException;

import cn.thens.xflow.func.Func0;
import cn.thens.xflow.func.Funcs;
import cn.thens.xflow.func.Predicate;
import cn.thens.xflow.func.Predicates;


/**
 * @author 7hens
 */
abstract class FlowTakeFirst<T> implements Flow.Operator<T, T> {
    @Override
    public Collector<T> apply(Emitter<T> emitter) {
        return reply -> {
            if (reply.isTerminated()) {
                try {
                    onNotFound(emitter, reply.error());
                } catch (Throwable e) {
                    emitter.error(e);
                }
                return;
            }
            try {
                if (test(reply.data())) {
                    emitter.emit(reply);
                    emitter.complete();
                }
            } catch (Throwable e) {
                emitter.error(e);
            }
        };
    }

    abstract void onNotFound(Emitter<T> emitter, Throwable error) throws Throwable;

    abstract boolean test(T data) throws Throwable;

    private static <T> FlowTakeFirst<T> create(final Predicate<T> predicate, final Func0<Reply<T>> defaultReply) {
        return new FlowTakeFirst<T>() {
            @Override
            void onNotFound(Emitter<T> emitter, Throwable error) throws Throwable {
                emitter.emit(defaultReply.invoke());
            }

            @Override
            boolean test(T data) throws Throwable {
                return predicate.test(data);
            }
        };
    }

    static <T> FlowTakeFirst<T> first() {
        return create(Predicates.always(), Funcs.result(Reply.error(new NoSuchElementException())));
    }

    static <T> FlowTakeFirst<T> firstOrDefault(final T defaultValue) {
        return create(Predicates.always(), Funcs.result(Reply.data(defaultValue)));
    }

    static <T> FlowTakeFirst<T> first(Predicate<T> predicate) {
        return create(predicate, Funcs.result(Reply.error(new NoSuchElementException())));
    }

    static <T> FlowTakeFirst<T> firstOrDefault(Predicate<T> predicate, T defaultValue) {
        return create(predicate, Funcs.result(Reply.data(defaultValue)));
    }
}
