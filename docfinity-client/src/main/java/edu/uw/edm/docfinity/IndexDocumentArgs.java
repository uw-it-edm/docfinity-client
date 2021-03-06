package edu.uw.edm.docfinity;

import lombok.Getter;

/** Encapsulates the arguments for indexing a document that already exists. */
public class IndexDocumentArgs extends IndexDocumentArgsBase<IndexDocumentArgs> {
    /** Identifier of document to index. */
    @Getter private final String documentId;

    public IndexDocumentArgs(String documentId) {
        this.documentId = documentId;
    }

    @Override
    protected IndexDocumentArgs self() {
        return this;
    }
}
