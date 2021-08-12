package edu.uw.edm.docfinity;

import edu.uw.edm.docfinity.models.DocumentIndexingDTO;
import java.util.List;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/** Encapsulates the result data from indexing operations. */
@Data
@RequiredArgsConstructor
public class IndexDocumentResult {
    /** Identifier of document indexed. */
    private final String id;

    /** Category name of document indexed. */
    private String category;

    /** Document type name of document indexed. */
    private String documentType;

    /** Metadata fields with values of document indexed. */
    private List<DocumentField> metadata;

    /** DocFinity's response to the index operation. */
    private DocumentIndexingDTO indexingDto;
}
