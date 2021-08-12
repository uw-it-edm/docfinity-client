package edu.uw.edm.docfinity;

import edu.uw.edm.docfinity.models.DocumentIndexingDTO;
import java.util.List;
import lombok.Data;
import lombok.RequiredArgsConstructor;

// TODO: comments
@Data
@RequiredArgsConstructor
public class IndexDocumentResult {
    private final String id;
    private final String documentType;
    private final String category;
    private List<DocumentField> metadata;
    private DocumentIndexingDTO indexingDto;
}
