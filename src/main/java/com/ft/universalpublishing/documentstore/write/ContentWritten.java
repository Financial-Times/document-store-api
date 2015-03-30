package com.ft.universalpublishing.documentstore.write;

import com.ft.content.model.Content;

public class ContentWritten {

    private final Mode mode;
    private final Content written;

    public ContentWritten(Mode mode, Content written) {
        this.mode = mode;
        this.written = written;
    }

    public Content getContent() {
        return written;
    }

    public Mode getMode() {
        return mode;
    }

    public static enum Mode {
        Created, Updated, Deleted
    }

    public static ContentWritten updated(Content content) {
        return new ContentWritten(Mode.Updated, content);

    }
    
    public static ContentWritten created(Content content) {
        return new ContentWritten(Mode.Created, content);
    }

    public static ContentWritten deleted(Content content) {
		return new ContentWritten(Mode.Deleted, content);
	}
}
