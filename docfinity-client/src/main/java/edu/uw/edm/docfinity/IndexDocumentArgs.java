package edu.uw.edm.docfinity;

import lombok.Getter;

/** Encapsulates the arguments for indexing a document. */
public class IndexDocumentArgs extends IndexDocumentArgsBase<IndexDocumentArgs> {
    /** Identifier of document to index. */
    @Getter private final String documentId;

    public IndexDocumentArgs(String documentId, String categoryName, String documentTypeName) {
        super(categoryName, documentTypeName);
        this.documentId = documentId;
    }

    @Override
    protected IndexDocumentArgs self() {
        return this;
    }
}
