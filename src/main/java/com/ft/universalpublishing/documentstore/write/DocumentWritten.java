package com.ft.universalpublishing.documentstore.write;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bson.Document;

@Getter
@RequiredArgsConstructor
public class DocumentWritten {

    private final Mode mode;
    private final Document document;

    public enum Mode {
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
