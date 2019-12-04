package com.ft.universalpublishing.documentstore.handler;

import com.ft.universalpublishing.documentstore.model.read.Context;
import com.ft.universalpublishing.documentstore.target.Target;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * A handler chain contains a list of {@link Handler}'s that a final {@link Target}
 * The role of this class is to configure a chain and execute it in the given order.
 */
public class HandlerChain {

    private List<Handler> handlers;
    private Target target;

    public HandlerChain() {
        handlers = new ArrayList<>();
    }


    public HandlerChain addHandlers(Handler... handlers) {
        this.handlers.addAll(Arrays.asList(handlers));
        return this;
    }

    public HandlerChain setTarget(Target target) {
        this.target = target;
        return this;
    }

    public Object execute(Context context) {
        handlers.forEach(handler -> handler.handle(context));
        return target.execute(context);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HandlerChain that = (HandlerChain) o;
        return Objects.equals(handlers, that.handlers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(handlers);
    }
}
