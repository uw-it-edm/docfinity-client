package edu.uw.edm.docfinity;

import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;
import edu.uw.edm.docfinity.models.DatasourceArgumentDTO;
import edu.uw.edm.docfinity.models.DatasourceArgumentPromptDTO;
import edu.uw.edm.docfinity.models.ExecuteDatasourceRequestDTO;
import edu.uw.edm.docfinity.models.ExecuteDatasourceResponseDTO;
import edu.uw.edm.docfinity.models.MetadataDTO;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Helper class to handle gathering prompt values and executing datasources for fields. */
public class DatasourceExecutor {
    private final DocFinityService service;
    private final Map<String, Function<ExecuteDatasourceArgs, Object>> defaultPrompts;

    public DatasourceExecutor(DocFinityService service) {
        Preconditions.checkNotNull(service, "service is required.");

        this.service = service;
        this.defaultPrompts = new HashMap<>();
        defaultPrompts.put("DOCUMENT.documentType", args -> args.getDocumentTypeName());
        defaultPrompts.put("DOCUMENT.category", args -> args.getCategory());
        defaultPrompts.put("DOCUMENT.id", args -> args.getDocumentId());
    }

    /**
    * Executes datasoures for all eligible fields based on the metadata definitions and the client
    * provided values.
    */
    public List<DocFinityDocumentField> executeDatasources(ExecuteDatasourceArgs executeArgs)
            throws IOException {
        Preconditions.checkNotNull(executeArgs, "executeArgs is required.");

        List<DocFinityDocumentField> result = new ArrayList<>();
        Multimap<String, Object> clientFields = executeArgs.getClientFields();
        Map<String, MetadataDTO> metadataMap = executeArgs.getMetadataMap();

        List<String> fieldsToRun =
                clientFields.entries().stream()
                        .map(field -> metadataMap.get(field.getKey()))
                        .filter(metadata -> !isNullOrEmpty(metadata.getResponsibilityMapping()))
                        .flatMap(metadata -> metadata.getResponsibilityMapping().stream())
                        .filter(fieldName -> !clientFields.containsKey(fieldName))
                        .collect(Collectors.toList());

        for (String fieldName : fieldsToRun) {
            MetadataDTO fieldMetadata = metadataMap.get(fieldName);
            List<DatasourceArgumentDTO> arguments =
                    buildDatasourceArgumentsForField(executeArgs, fieldMetadata);

            ExecuteDatasourceRequestDTO datasourceRequest = new ExecuteDatasourceRequestDTO();
            datasourceRequest.setDocumentId(executeArgs.getDocumentId());
            datasourceRequest.setDocumentTypeId(executeArgs.getDocumentTypeId());
            datasourceRequest.setMetadataId(fieldMetadata.getId());
            datasourceRequest.setArguments(arguments);

            List<ExecuteDatasourceResponseDTO> responses =
                    this.service.executeDatasource(datasourceRequest);

            if (responses.size() > 1) {
                throwInvalidDatasourceResultValueException(
                        fieldName, responses, executeArgs.getDocumentTypeName());
            }

            Object fieldValue = responses.stream().findFirst().get().getValue();
            result.add(new DocFinityDocumentField(fieldMetadata.getName(), fieldValue));
        }

        return result;
    }

    private List<DatasourceArgumentDTO> buildDatasourceArgumentsForField(
            ExecuteDatasourceArgs args, MetadataDTO fieldMetadata) {

        List<DatasourceArgumentDTO> arguments = new ArrayList<>();
        for (DatasourceArgumentPromptDTO prompt : fieldMetadata.getDatasourcePrompts()) {
            String promptName = prompt.getArgumentName();

            if (defaultPrompts.containsKey(promptName)) {
                Object promptValue = defaultPrompts.get(promptName).apply(args);
                arguments.add(new DatasourceArgumentDTO(promptName, promptValue, MetadataTypeEnum.STRING));
            } else {
                MetadataDTO promptMetadata = args.getMetadataMap().get(promptName);
                MetadataTypeEnum promptDataType = promptMetadata.getDataType();
                Optional<Object> promptValue = args.getClientFields().get(promptName).stream().findFirst();

                if (promptMetadata.isAllowMultipleValues()) {
                    throwMultiSelectDatasourcePromptNotSupportedException(
                            fieldMetadata.getName(), promptMetadata.getName(), args.getDocumentTypeName());
                } else if (!promptValue.isPresent()) {
                    throwMissingDatasourcePromptValueException(
                            fieldMetadata.getName(), promptMetadata.getName(), args.getDocumentTypeName());
                }

                arguments.add(new DatasourceArgumentDTO(promptName, promptValue.get(), promptDataType));
            }
        }

        return arguments;
    }

    private void throwMultiSelectDatasourcePromptNotSupportedException(
            String datasourceField, String datasourcePrompt, String documentTypeName) {
        throw new IllegalStateException(
                String.format(
                        "Multi-select fields for datasource prompts are not supported. Datasource field: '%s', prompt field: '%s', document type: '%s'.",
                        datasourceField, datasourcePrompt, documentTypeName));
    }

    private void throwMissingDatasourcePromptValueException(
            String datasourceField, String datasourcePrompt, String documentTypeName) {
        throw new IllegalStateException(
                String.format(
                        "Datasource prompt '%s' for field '%s' in document type '%s' is missing in client metadata.",
                        datasourcePrompt, datasourceField, documentTypeName));
    }

    private void throwInvalidDatasourceResultValueException(
            String datasourceField,
            List<ExecuteDatasourceResponseDTO> datasourceResponses,
            String documentTypeName) {

        String datasourceValues =
                datasourceResponses.stream()
                        .map(d -> d.getValue())
                        .map(s -> s == null ? "" : s.toString())
                        .collect(Collectors.joining(", "));

        throw new IllegalStateException(
                String.format(
                        "Returning lists from datasources is not supported. Field '%s' in document type '%s'. Returned values: %s.",
                        datasourceField, documentTypeName, datasourceValues));
    }

    private <T> boolean isNullOrEmpty(List<T> list) {
        return list == null || list.isEmpty();
    }
}
