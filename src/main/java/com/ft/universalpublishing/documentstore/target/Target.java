package com.ft.universalpublishing.documentstore.target;

import com.ft.universalpublishing.documentstore.model.read.Context;

/**
 * A Target represents the last step of a chain of handlers.
 */
public interface Target {

    /**
     * Executes the final step of a chain of handlers in the given context
     *
     * @param context context
     * @return the result
     */
    Object execute(Context context);
}
