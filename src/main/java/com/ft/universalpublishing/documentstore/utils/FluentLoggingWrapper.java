package com.ft.universalpublishing.documentstore.utils;

import static com.ft.api.util.transactionid.TransactionIdUtils.TRANSACTION_ID_HEADER;
import static com.ft.membership.logging.Operation.operation;
import static com.ft.universalpublishing.documentstore.utils.FluentLoggingUtils.ACCEPT;
import static com.ft.universalpublishing.documentstore.utils.FluentLoggingUtils.CLIENT;
import static com.ft.universalpublishing.documentstore.utils.FluentLoggingUtils.COLLECTION;
import static com.ft.universalpublishing.documentstore.utils.FluentLoggingUtils.CONTENT_TYPE;
import static com.ft.universalpublishing.documentstore.utils.FluentLoggingUtils.EXCEPTION;
import static com.ft.universalpublishing.documentstore.utils.FluentLoggingUtils.METHOD;
import static com.ft.universalpublishing.documentstore.utils.FluentLoggingUtils.PATH;
import static com.ft.universalpublishing.documentstore.utils.FluentLoggingUtils.RUNBOOK_URI;
import static com.ft.universalpublishing.documentstore.utils.FluentLoggingUtils.STACKTRACE;
import static com.ft.universalpublishing.documentstore.utils.FluentLoggingUtils.STATUS;
import static com.ft.universalpublishing.documentstore.utils.FluentLoggingUtils.TRANSACTION_ID;
import static com.ft.universalpublishing.documentstore.utils.FluentLoggingUtils.TRANSACTION_ID_START_PART;
import static com.ft.universalpublishing.documentstore.utils.FluentLoggingUtils.URI;
import static com.ft.universalpublishing.documentstore.utils.FluentLoggingUtils.USER_AGENT;
import static java.lang.String.valueOf;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static org.apache.commons.lang.exception.ExceptionUtils.getStackTrace;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ft.membership.logging.IntermediateYield;
import com.ft.membership.logging.Operation;
import com.ft.universalpublishing.documentstore.model.read.Context;

public class FluentLoggingWrapper {
    private final static Logger logger = LoggerFactory.getLogger(FluentLoggingWrapper.class);

    public static final String SYSTEM_CODE = "systemcode";
    public static final String APPLICATION_NAME = "document-store-api";
    
    private Map<String, Object> items;
    private String methodName;
    private String loggingClassName;
    
    public FluentLoggingWrapper() {
        items = new HashMap<>();
    }

    public FluentLoggingWrapper withField(String fieldName, Object fieldValue) {
        if (isBlank(fieldName) || isNull(fieldValue) || isBlank(valueOf(fieldValue))) {
            return this;
        }
        items.put(fieldName, fieldValue);
        return this;
    }
    
    public FluentLoggingWrapper withClassName(String loggingClassName) {
        this.loggingClassName = loggingClassName;
        return this;
    }

    public FluentLoggingWrapper withMetodName(final String name) {
        methodName = name;
        return this;
    }

    public FluentLoggingWrapper withException(Throwable t) {
        if (nonNull(t)) {
            withField(EXCEPTION, t.getLocalizedMessage());
            if (logger.isDebugEnabled()) {
                withField(STACKTRACE, getStackTrace(t));
            }
        } else {
            withField(EXCEPTION, "Exception was null");
        }
        return this;
    }

    public FluentLoggingWrapper withRequest(Context context, String method, String path) {
        withField(METHOD, method);
        withField(PATH, path);
        if (nonNull(context.getContentMap())) {
            withField(CONTENT_TYPE, context.getContentMap().get("type"));
        }
        withField(COLLECTION, context.getCollection());
        return this;
    }
    
    public FluentLoggingWrapper withUriInfo(URI uri) {
        if (nonNull(uri)) {
            withField(URI, uri.toString());
            withField(PATH, uri.getPath());
        }
        return this;
    }

    public FluentLoggingWrapper withUriInfo(UriInfo uri) {
        if (nonNull(uri)) {
            withField(URI, uri.getAbsolutePath().toString());
            withField(PATH, uri.getPath());
        }
        return this;
    }

    public FluentLoggingWrapper withResponse(Response response) {
        if (nonNull(response)) {
            withField(STATUS, valueOf(response.getStatus()));
            withField(CLIENT, response.getClass().getCanonicalName());
        }
        withOutboundHeaders(response);
        return this;
    }

    private FluentLoggingWrapper withOutboundHeaders(Response response) {
        String contentTypeHeader = flattenHeaderToString(response, CONTENT_TYPE);
        withField(CONTENT_TYPE, contentTypeHeader);
        withField(USER_AGENT, getOutboundUserAgentHeader());
        withField(ACCEPT, APPLICATION_JSON_TYPE.toString());
        return this;
    }
    
    public FluentLoggingWrapper withTransactionId(final String transactionId) {
        String tid = null;
        if (!isBlank(transactionId) && transactionId.contains(TRANSACTION_ID_START_PART)) {
            tid = transactionId;
        } else if (!isBlank(transactionId)) {
            tid = TRANSACTION_ID_START_PART + transactionId;
        }
        withField(TRANSACTION_ID_HEADER, tid);
        withField(TRANSACTION_ID, tid);
        return this;
    }
    
    public IntermediateYield build() {
        Operation operationJson = operation(methodName).jsonLayout().initiate(loggingClassName);
        IntermediateYield iy = operationJson.logIntermediate();
        iy.yielding(FluentLoggingUtils.CLASS, loggingClassName);
        iy.yielding(SYSTEM_CODE, APPLICATION_NAME);
        iy.yielding(items);
        items = new HashMap<>();
        return iy;
    }

    private static String getOutboundUserAgentHeader() {
        String gitTag = System.getProperty("gitTag");
        String userAgentValue;
        if (isNotEmpty(gitTag)) {
            userAgentValue = APPLICATION_NAME + "/" + gitTag + " (+" + RUNBOOK_URI + ")";
        } else {
            userAgentValue = APPLICATION_NAME + " (+" + RUNBOOK_URI + ")";
        }
        return userAgentValue;
    }
    
    private static String flattenHeaderToString(Response response, String headerKey) {
        if (isNull(response.getHeaders())) {
            return EMPTY;
        }
        List<Object> headersPerHeaderKey = response.getHeaders().get(headerKey);
        if (nonNull(headersPerHeaderKey)) {
            return headersPerHeaderKey.stream().map(Object::toString).collect(joining(";"));
        }    

        return EMPTY;
    }

}
