package edu.uw.edm.docfinity;

import lombok.Data;
import lombok.RequiredArgsConstructor;

/** Encapsulates the data that DocFinityClient returns after creating a document. */
@Data
@RequiredArgsConstructor
public class CreateDocumentResult {
    /** Identifier of the document that was created. */
    private final String documentId;
}
