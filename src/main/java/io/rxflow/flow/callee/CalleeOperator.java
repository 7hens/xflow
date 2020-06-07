package io.rxflow.flow.callee;

import io.rxflow.flow.caller.Caller;
import io.rxflow.flow.reply.Reply;

public interface CalleeOperator<Up, Dn> {
    void apply(Reply<Up> reply, Caller<Dn> caller) throws Throwable;
}
