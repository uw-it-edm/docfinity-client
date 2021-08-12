package edu.uw.edm.docfinity;

import lombok.Getter;

/** Encapsulates the arguments for updating a document. */
public class UpdateDocumentArgs extends IndexDocumentArgs<UpdateDocumentArgs> {
    /** Identifier of document to index. */
    @Getter private final String documentId;

    public UpdateDocumentArgs(String documentId, String categoryName, String documentTypeName) {
        super(categoryName, documentTypeName);
        this.documentId = documentId;
    }

    @Override
    protected UpdateDocumentArgs self() {
        return this;
    }
}
