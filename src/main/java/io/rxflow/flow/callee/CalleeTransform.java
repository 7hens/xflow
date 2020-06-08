package io.rxflow.flow.callee;

import io.rxflow.flow.caller.Caller;
import io.rxflow.flow.reply.Reply;
import io.rxflow.flow.reply.ReplyWrapper;

public class CalleeTransform<Up, Dn> extends Callee<Dn> {
    private final Callee<Up> upCallee;
    private final CalleeOperator<Up, Dn> operator;

    public CalleeTransform(Callee<Up> upCallee, CalleeOperator<Up, Dn> operator) {
        this.upCallee = upCallee;
        this.operator = operator;
    }

    @Override
    public void reply(Caller<Dn> caller) {
        upCallee.reply(reply -> {
            try {
                operator.apply(reply, caller);
            } catch (Throwable e) {
                caller.receive(Reply.of(e));
            }
        });
    }
}
