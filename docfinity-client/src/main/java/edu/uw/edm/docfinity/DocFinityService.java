package edu.uw.edm.docfinity;

import edu.uw.edm.docfinity.models.DocumentIndexingDTO;
import edu.uw.edm.docfinity.models.DocumentTypeDTOSearchResult;
import edu.uw.edm.docfinity.models.ExecuteDatasourceRequestDTO;
import edu.uw.edm.docfinity.models.ExecuteDatasourceResponseDTO;
import edu.uw.edm.docfinity.models.MetadataDTO;
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
    * Represents call to '/servlet/upload' to upload file as byte array.
    *
    * @param content Document content as byte array.
    * @param name Name of document.
    */
    String uploadDocument(byte[] content, String name) throws IOException;

    /**
    * Represents a call to '/indexing/data' to retrieve the indexing data for the document id
    * specified.
    */
    DocumentIndexingDTO getDocumentIndexingData(String documentId) throws IOException;

    /**
    * Represents a call to '/indexing/controls' to retrieve the metadata of document including
    * datasource information.
    *
    * @param documentTypeId Id of document type to retrieve metadata for.
    * @param documentId Id of document to retrieve metadata for.
    */
    List<MetadataDTO> getDocumentMetadata(String documentTypeId, String documentId)
            throws IOException;

    /** Represents a call to '/indexing/executeDatasource' to execute a datasource for a field. */
    List<ExecuteDatasourceResponseDTO> executeDatasource(ExecuteDatasourceRequestDTO request)
            throws IOException;

    /** Represents call to 'webservices/rest/indexing/index/commit' to index and commit a document. */
    List<DocumentIndexingDTO> indexDocuments(DocumentIndexingDTO... documents) throws IOException;

    /** Represents call to 'webservices/rest/indexing/reindex' to reindex document. */
    List<DocumentIndexingDTO> reindexDocuments(DocumentIndexingDTO... documents) throws IOException;

    /**
    * Represents call to 'webservices/rest/document/delete' to soft-delete documents from DocFinity
    */
    void deleteDocuments(String... documentIds) throws IOException;
}
