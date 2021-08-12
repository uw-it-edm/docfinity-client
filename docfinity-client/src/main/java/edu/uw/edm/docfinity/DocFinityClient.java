package edu.uw.edm.docfinity;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import edu.uw.edm.docfinity.models.DocumentIndexingDTO;
import edu.uw.edm.docfinity.models.DocumentIndexingMetadataDTO;
import edu.uw.edm.docfinity.models.DocumentTypeDTOSearchResult;
import edu.uw.edm.docfinity.models.MetadataDTO;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
    * Uploads, indexes and commits a document to DocFinity.
    *
    * @param args Class that encapsulates arguments for create document operation.
    */
    public IndexDocumentResult uploadIndexAndCommitDocument(FileIndexDocumentArgs args)
            throws Exception {
        Preconditions.checkNotNull(args, "args is required.");
        args.validate();

        // 1. Get the document type id from the category and document names.
        String documentTypeId = getDocumentTypeId(args.getCategoryName(), args.getDocumentTypeName());
        log.info("Retrieved document type id: {}", documentTypeId);

        // 2. Upload file.
        String documentId = uploadFile(args);
        log.info("File uploaded, document id: {}", documentId);

        try {
            IndexDocumentArgs updateArgs =
                    new IndexDocumentArgs(documentId, args.getCategoryName(), args.getDocumentTypeName());
            updateArgs.setMetadata(args.getMetadata());

            return this.indexAndCommitInternal(documentTypeId, updateArgs);
        } catch (Exception e) {
            // 6. If there is an error after the file has been upload it, need to delete it from server.
            this.tryDeleteDocument(documentId);
            throw e;
        }
    }

    /**
    * Indexes and commits a document to DocFinity.
    *
    * @param args Class that encapsulates arguments for index document operation.
    */
    public IndexDocumentResult indexAndCommitDocument(IndexDocumentArgs args) throws Exception {
        Preconditions.checkNotNull(args, "args is required.");
        args.validate();

        // Get the document type id from the category and document names.
        String documentTypeId = getDocumentTypeId(args.getCategoryName(), args.getDocumentTypeName());
        log.info("Retrieved document type id: {}", documentTypeId);

        return indexAndCommitInternal(documentTypeId, args);
    }

    private IndexDocumentResult indexAndCommitInternal(String documentTypeId, IndexDocumentArgs args)
            throws Exception {
        Preconditions.checkNotNull(args, "args is required.");
        args.validate();

        String documentId = args.getDocumentId();

        // 1. Get all metadata prompts and validate inputs
        Map<String, MetadataDTO> metadata = getDocumentMetadataMap(documentTypeId, documentId);
        IndexingMetadataBuilder builder =
                new IndexingMetadataBuilder(args.getDocumentTypeName(), metadata, Arrays.asList())
                        .addValues(args.getMetadata());

        // 2. Execute datasources.
        DatasourceExecutor executor = new DatasourceExecutor(this.service);
        ExecuteDatasourceArgs executeArgs = new ExecuteDatasourceArgs();
        executeArgs.setDocumentId(documentId);
        executeArgs.setDocumentTypeId(documentTypeId);
        executeArgs.setDocumentTypeName(args.getDocumentTypeName());
        executeArgs.setCategory(args.getCategoryName());
        executeArgs.setClientFields(args.getMetadata());
        executeArgs.setMetadataMap(metadata);
        executor.executeDatasources(executeArgs).stream().forEach(field -> builder.addValue(field));

        // 3. Index and commit the document using the calculated values from datasources.
        builder.validateAllRequiredFieldsHaveValue();
        List<DocumentIndexingMetadataDTO> indexingDtos = builder.build();
        DocumentIndexingDTO indexingDto =
                new DocumentIndexingDTO(documentTypeId, documentId, indexingDtos);

        DocumentIndexingDTO indexedDto =
                this.service.indexDocuments(indexingDto).stream().findFirst().get();
        return buildIndexResult(args, indexedDto);
    }

    /**
    * Reindexes a document to DocFinity.
    *
    * @param args Class that encapsulates arguments for reindex document operation.
    */
    public IndexDocumentResult reindexDocument(IndexDocumentArgs args) throws Exception {
        Preconditions.checkNotNull(args, "args is required.");
        args.validate();

        String documentId = args.getDocumentId();

        // 1. Get the document type id from the category and document names.
        String documentTypeId = getDocumentTypeId(args.getCategoryName(), args.getDocumentTypeName());
        log.info("Retrieved document type id: {}", documentTypeId);

        // 2. Get all metadata prompts and validate inputs
        Map<String, MetadataDTO> metadata = getDocumentMetadataMap(documentTypeId, documentId);
        DocumentIndexingDTO indexingData = service.getDocumentIndexingData(documentId);

        IndexingMetadataBuilder builder =
                new IndexingMetadataBuilder(
                                args.getDocumentTypeName(), metadata, indexingData.getIndexingMetadata())
                        .addValues(args.getMetadata());

        // 3. Execute datasources.
        DatasourceExecutor executor = new DatasourceExecutor(this.service);
        ExecuteDatasourceArgs executeArgs = new ExecuteDatasourceArgs();
        executeArgs.setDocumentId(documentId);
        executeArgs.setDocumentTypeId(documentTypeId);
        executeArgs.setDocumentTypeName(args.getDocumentTypeName());
        executeArgs.setCategory(args.getCategoryName());
        executeArgs.setClientFields(args.getMetadata());
        executeArgs.setMetadataMap(metadata);
        executor.executeDatasources(executeArgs).stream().forEach(field -> builder.addValue(field));

        // 4. Reindex the document using the calculated values from datasources.
        builder.validateRequiredFieldsPresentHaveValue();
        List<DocumentIndexingMetadataDTO> indexingDtos = builder.build();
        DocumentIndexingDTO indexingDto =
                new DocumentIndexingDTO(documentTypeId, documentId, indexingDtos);
        indexingDto.setMetadataLoaded(true); // treat this as a partial reindex

        DocumentIndexingDTO indexedDTO =
                this.service.reindexDocuments(indexingDto).stream().findFirst().get();
        return buildIndexResult(args, indexedDTO);
    }

    private IndexDocumentResult buildIndexResult(
            IndexDocumentArgsBase<?> args, DocumentIndexingDTO indexingDto) {
        IndexDocumentResult result = new IndexDocumentResult(indexingDto.getDocumentId());
        result.setCategory(args.getCategoryName());
        result.setDocumentType(args.getDocumentTypeName());
        result.setIndexingDto(indexingDto);

        Multimap<String, Object> fieldsMap = ArrayListMultimap.create();
        for (DocumentIndexingMetadataDTO metadataDto : indexingDto.getIndexingMetadata()) {
            fieldsMap.put(metadataDto.getMetadataName(), metadataDto.getValue());
        }

        // TODO: handle and test case where the indexing value is null
        List<DocumentField> fields =
                fieldsMap.asMap().entrySet().stream()
                        .map(e -> new DocumentField(e.getKey(), new ArrayList<>(e.getValue())))
                        .collect(Collectors.toList());

        result.setMetadata(fields);

        return result;
    }

    private Map<String, MetadataDTO> getDocumentMetadataMap(String documentTypeId, String documentId)
            throws IOException {

        List<MetadataDTO> metadata = service.getDocumentMetadata(documentTypeId, documentId);

        return metadata.stream().collect(Collectors.toMap(MetadataDTO::getName, m -> m));
    }

    private String uploadFile(FileIndexDocumentArgs args) throws IOException {
        String documentId;
        if (args.getFile() != null) {
            documentId = this.service.uploadDocument(args.getFile());
        } else {
            documentId = this.service.uploadDocument(args.getFileContent(), args.getFileName());
        }
        return documentId;
    }

    private void tryDeleteDocument(String documentId) {
        try {
            this.service.deleteDocuments(documentId);
            log.info("Document deleted due to indexing error, id: {}", documentId);
        } catch (IOException e) {
            log.error("Failed to delete document '%s'. Error Message: ", documentId, e.getMessage());
        }
    }

    private String getDocumentTypeId(String categoryName, String documentTypeName)
            throws IOException {
        DocumentTypeDTOSearchResult documentTypes =
                this.service.getDocumentTypes(categoryName, documentTypeName);

        Preconditions.checkNotNull(documentTypes, "getDocumentTypes() result is null.");
        int count = documentTypes.getTotalAvailable();

        if (count == 1) {
            return documentTypes.getResults().get(0).getId();
        } else if (count > 1) {
            throw new IllegalStateException(
                    String.format(
                            "Multiple document types with category '%s' and name '%s' found in server.",
                            categoryName, documentTypeName));
        } else {
            throw new IllegalStateException(
                    String.format(
                            "Document type with category '%s' and name '%s' does not exist in server.",
                            categoryName, documentTypeName));
        }
    }
}
