package com.ft.universalpublishing.documentstore.write;


import org.bson.Document;

public class DocumentWritten {

    private final Mode mode;
    private final Document written;

    public DocumentWritten(Mode mode, Document written) {
        this.mode = mode;
        this.written = written;
    }

    public Document getDocument() {
        return written;
    }

    public Mode getMode() {
        return mode;
    }

    public static enum Mode {
        Created, Updated, Deleted
    }

    public static DocumentWritten updated(Document document) {
        return new DocumentWritten(Mode.Updated, document);

    }
    
    public static DocumentWritten created(Document content) {
        return new DocumentWritten(Mode.Created, content);
    }

    public static DocumentWritten deleted(Document content) {
		return new DocumentWritten(Mode.Deleted, content);
	}
}
