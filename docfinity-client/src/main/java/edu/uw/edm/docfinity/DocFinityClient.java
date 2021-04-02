package edu.uw.edm.docfinity;

import com.google.common.base.Preconditions;
import edu.uw.edm.docfinity.models.DocumentIndexingDTO;
import edu.uw.edm.docfinity.models.DocumentTypeDTOSearchResult;
import edu.uw.edm.docfinity.models.DocumentTypeMetadataDTO;
import edu.uw.edm.docfinity.models.EntryControlWrapperDTO;
import edu.uw.edm.docfinity.models.ParameterPromptDTO2;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
    */
    public CreateDocumentResult createDocument(
            File file, String categoryName, String documentTypeName, Map<String, Object> metadata)
            throws IOException {

        Preconditions.checkNotNull(file, "file is required.");
        Preconditions.checkNotNull(categoryName, "categoryName is required.");
        Preconditions.checkNotNull(documentTypeName, "documentTypeName is required.");
        Preconditions.checkNotNull(metadata, "metadata is required.");
        Preconditions.checkArgument(file.exists(), "file must exist.");

        // 1. Get the document type id from the category and document names.
        String documentTypeId = getDocumentTypeId(categoryName, documentTypeName);
        log.info("Retrieved document type id: {}", documentTypeId);

        // 2. Get the metadata objects from the document type id.
        List<DocumentTypeMetadataDTO> metadataDefinitions = getMetadataDefinitions(documentTypeId);

        // 3. Upload file.
        String documentId = this.service.uploadDocument(file);
        log.info("File uploaded, document id: {}", documentId);

        // 4. Get control prompts that executes data sources from the partial client metadata.
        DocFinityDtoMapper dtoMapper = new DocFinityDtoMapper(documentTypeId, documentId, metadata);

        EntryControlWrapperDTO controlsRequest =
                dtoMapper.buildControlDtoFromMetadata(metadataDefinitions);
        List<ParameterPromptDTO2> controlsResponse = this.service.getIndexingControls(controlsRequest);

        // 5. Index and commit the document using the calculated values from prompts.
        DocumentIndexingDTO indexRequest =
                dtoMapper.buildIndexingDtoFromControlPromptDtos(controlsResponse);
        this.service.indexDocuments(indexRequest);

        // TODO: 6. Delete file if indexing fails

        // Build result to return to client.
        return new CreateDocumentResult(documentId);
    }

    private List<DocumentTypeMetadataDTO> getMetadataDefinitions(String documentTypeId)
            throws IOException {
        List<DocumentTypeMetadataDTO> metadataDefinitions =
                this.service.getDocumentTypeMetadata(documentTypeId);

        // TODO: Error check and validate reponse.
        return metadataDefinitions;
    }

    private String getDocumentTypeId(String categoryName, String documentTypeName)
            throws IOException {
        DocumentTypeDTOSearchResult documentTypes =
                this.service.getDocumentTypes(categoryName, documentTypeName);

        // TODO: Error check and validate response.
        return documentTypes.getResults().get(0).getId();
    }
}
