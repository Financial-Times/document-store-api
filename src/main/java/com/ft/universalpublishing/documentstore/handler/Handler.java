package com.ft.universalpublishing.documentstore.handler;

import com.ft.universalpublishing.documentstore.model.read.Context;

/**
 * A Handler represents a median step for handling an operation.
 */
public interface Handler {

    /**
     * Handles the step in the given context
     *
     * @param context the context
     */
    void handle(Context context);
}
