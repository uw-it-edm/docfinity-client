package edu.uw.edm.docfinity;

import edu.uw.edm.docfinity.models.DocumentIndexingDTO;
import edu.uw.edm.docfinity.models.DocumentTypeDTOSearchResult;
import edu.uw.edm.docfinity.models.DocumentTypeMetadataDTO;
import edu.uw.edm.docfinity.models.EntryControlWrapperDTO;
import edu.uw.edm.docfinity.models.ParameterPromptDTO2;
import java.io.File;
import java.io.IOException;
import java.util.List;

/** Abstracts the interaction with DocFinity REST API. */
public interface DocFinityService {
    /**
    * Represents call to '/webservices/rest/documentType' to retrieve document types.
    *
    * @param categoryName Category name to use in query filter.
    * @param documentTypeName Document type name to use in query filter.
    */
    DocumentTypeDTOSearchResult getDocumentTypes(String categoryName, String documentTypeName)
            throws IOException;

    /**
    * Represents call to '/servlet/upload' to upload file.
    *
    * @param file File to upload.
    * @return The id of the new document.
    */
    String uploadDocument(File file) throws IOException;

    /**
    * Represents call to 'webservices/rest/documentType/metadata' to retrieve metadata objects for a
    * given document type.
    *
    * @param documentTypeId Document type id.
    */
    List<DocumentTypeMetadataDTO> getDocumentTypeMetadata(String documentTypeId) throws IOException;

    /**
    * Represents call to 'webservices/rest/indexing/controls' to retrieve values of all metadata
    * objects after running data sources for a given document.
    */
    List<ParameterPromptDTO2> getIndexingControls(EntryControlWrapperDTO requestData)
            throws IOException;

    /** Represents call to 'webservices/rest/indexing/index/commit' to index and commit a document. */
    List<DocumentIndexingDTO> indexDocuments(DocumentIndexingDTO... documentIndexingDTOs)
            throws IOException;
}
