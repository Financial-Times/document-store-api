package com.ft.universalpublishing.documentstore.write;

import java.util.Map;

public class DocumentWritten {

    private final Mode mode;
    //i.e. Json
    private final Map<String, Object> written;

    public DocumentWritten(Mode mode, Map<String, Object> written) {
        this.mode = mode;
        this.written = written;
    }

    public Map<String, Object> getDocument() {
        return written;
    }

    public Mode getMode() {
        return mode;
    }

    public static enum Mode {
        Created, Updated, Deleted
    }

    public static DocumentWritten updated(Map<String, Object> content) {
        return new DocumentWritten(Mode.Updated, content);

    }
    
    public static DocumentWritten created(Map<String, Object> content) {
        return new DocumentWritten(Mode.Created, content);
    }

    public static DocumentWritten deleted(Map<String, Object> content) {
		return new DocumentWritten(Mode.Deleted, content);
	}
}
