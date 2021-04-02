package edu.uw.edm.docfinity;

import edu.uw.edm.docfinity.models.DocumentTypesResponse;
import java.io.File;
import java.io.IOException;
import java.util.Map;

/** Abstracts the create and update operations for DocFinity documents. */
public class DocFinityClient {
    private final DocFinityService service;

    /**
    * Creates a new instance of the DocFinityClient.
    *
    * @param url The DocFinity base url.
    * @param apiKey The API key to use to interact with DocFinity.
    */
    public DocFinityClient(String url, String apiKey) {
        this.service = new DocFinityServiceImpl(url, apiKey, null);
    }

    /**
    * Creates a new instance of the DocFinityClient.
    *
    * @param url The DocFinity base url.
    * @param apiKey The API key to use to interact with DocFinity.
    * @param auditUser DocFinity account username to use for auditing calls and document history.
    */
    public DocFinityClient(String url, String apiKey, String auditUser) {
        this.service = new DocFinityServiceImpl(url, apiKey, auditUser);
    }

    /**
    * Creates a new instance of the DocFinityClient, intended for testing purposes.
    *
    * @param service An implementation of the DocFinityService.
    */
    public DocFinityClient(DocFinityService service) {
        this.service = service;
    }

    /**
    * Uploads and indexes a document to DocFinity.
    *
    * @param file File to upload.
    * @param categoryName Category name to index document.
    * @param documentTypeName Document type name to index document.
    * @param metadata Map of metadata object names with their value to use when indexing.
    * @throws IOException
    */
    public CreateDocumentResult createDocument(
            File file, String categoryName, String documentTypeName, Map<String, Object> metadata)
            throws IOException {
        DocumentTypesResponse documentTypes =
                this.service.getDocumentTypes(categoryName, documentTypeName);

        return new CreateDocumentResult(documentTypes.getResults().get(0).getId());
    }
}
