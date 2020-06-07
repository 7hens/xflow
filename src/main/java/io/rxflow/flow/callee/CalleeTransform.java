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
        upCallee.reply(new Caller<Up>() {
            @Override
            public void receive(Reply<Up> reply) {
                Caller<Dn> wrapCaller = downReply -> {
                    caller.receive(new ReplyWrapper<Dn>() {
                        @Override
                        protected Reply<Dn> baseReply() {
                            return downReply;
                        }

                        @Override
                        public Callee<Dn> callee() {
                            return new CalleeTransform<>(reply.callee(), operator);
                        }
                    });
                };
                try {
                    operator.apply(reply, wrapCaller);
                } catch (Throwable e) {
                    wrapCaller.receive(Reply.of(e));
                }
            }
        });
    }
}
