package edu.uw.edm.docfinity;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import edu.uw.edm.docfinity.models.DocumentIndexingDTO;
import edu.uw.edm.docfinity.models.DocumentIndexingMetadataDTO;
import edu.uw.edm.docfinity.models.DocumentTypeMetadataDTO;
import edu.uw.edm.docfinity.models.EntryControlWrapperDTO;
import edu.uw.edm.docfinity.models.ParameterPromptDTO2;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class DocFinityDtoMapperTest {
    private static final String documentTypeId = "testDocumentTypeId";
    private static final String documentId = "testDocumentId";

    private DocFinityDtoMapper buildMapper(Map<String, Object> metadataMap) {
        Multimap<String, Object> metadata = ArrayListMultimap.create();
        metadataMap.entrySet().stream()
                .forEach(entry -> metadata.put(entry.getKey(), entry.getValue()));
        return new DocFinityDtoMapper(documentTypeId, documentId, metadata);
    }

    @Test
    public void buildControlDtoShouldIncludeMultiSelectValues() {
        // arrange
        List<DocumentTypeMetadataDTO> metadataDefinitions =
                Arrays.asList(new DocumentTypeMetadataDTO("123", "Field"));
        Multimap<String, Object> userValues = ArrayListMultimap.create();
        userValues.put("Field", "Value1");
        userValues.put("Field", "Value2");
        DocFinityDtoMapper mapper = new DocFinityDtoMapper(documentTypeId, documentId, userValues);

        // act
        EntryControlWrapperDTO result = mapper.buildControlDtoFromMetadata(metadataDefinitions);

        // assert
        assertThat(
                result.getData(),
                is(
                        Arrays.asList(
                                new DocumentIndexingMetadataDTO("123", "Value1"),
                                new DocumentIndexingMetadataDTO("123", "Value2"))));
    }

    @Test
    public void buildIndexingDtoShouldRemoveNullValuesFromPrompts() {
        // arrange
        DocFinityDtoMapper mapper = buildMapper(ImmutableMap.of("Field1", "User Value"));
        List<ParameterPromptDTO2> promptDtos =
                Arrays.asList(
                        new ParameterPromptDTO2("123", "Field1", "DataSource Value"),
                        new ParameterPromptDTO2("456", "Field2", null));
        // act
        DocumentIndexingDTO result = mapper.buildIndexingDtoFromControlPromptDtos(promptDtos);

        // assert
        assertThat(
                result.getDocumentIndexingMetadataDtos(),
                is(Arrays.asList(new DocumentIndexingMetadataDTO("123", "DataSource Value"))));
    }

    @Test
    public void buildIndexingDtoShouldExpandMultiSelectValuesFromPrompts() {
        // arrange
        DocFinityDtoMapper mapper = buildMapper(ImmutableMap.of("Field1", "User Value"));
        ParameterPromptDTO2 prompt = new ParameterPromptDTO2("123", "Test Field");
        prompt.setStrDefaultValue(new String[] {"Value1", "Value2"});

        // act
        DocumentIndexingDTO result =
                mapper.buildIndexingDtoFromControlPromptDtos(Arrays.asList(prompt));

        // assert
        assertThat(
                result.getDocumentIndexingMetadataDtos(),
                is(
                        Arrays.asList(
                                new DocumentIndexingMetadataDTO("123", "Value1"),
                                new DocumentIndexingMetadataDTO("123", "Value2"))));
    }
}
