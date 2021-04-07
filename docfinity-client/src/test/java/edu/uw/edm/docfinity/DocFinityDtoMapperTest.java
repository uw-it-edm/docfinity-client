package edu.uw.edm.docfinity;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import edu.uw.edm.docfinity.models.DatasourceRunningDTO;
import edu.uw.edm.docfinity.models.DocumentIndexingDTO;
import edu.uw.edm.docfinity.models.DocumentIndexingMetadataDTO;
import edu.uw.edm.docfinity.models.DocumentServerMetadataDTO;
import edu.uw.edm.docfinity.models.DocumentTypeMetadataDTO;
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
    public void buildDatasourceDtoShouldIncludeMultiSelectValues() {
        // arrange
        List<DocumentTypeMetadataDTO> metadataDefinitions =
                Arrays.asList(new DocumentTypeMetadataDTO("123", "Field"));
        Multimap<String, Object> userValues = ArrayListMultimap.create();
        userValues.put("Field", "Value1");
        userValues.put("Field", "Value2");
        DocFinityDtoMapper mapper = new DocFinityDtoMapper(documentTypeId, documentId, userValues);

        // act
        DatasourceRunningDTO result = mapper.buildDatasourceDtoFromMetadata(metadataDefinitions);

        // assert
        assertThat(
                result.getData(),
                is(
                        Arrays.asList(
                                new DocumentIndexingMetadataDTO("123", "Value1"),
                                new DocumentIndexingMetadataDTO("123", "Value2"))));
    }

    @Test
    public void buildIndexingDtoShouldRemoveNullValuesFromServerMetadata() {
        // arrange
        DocFinityDtoMapper mapper = buildMapper(ImmutableMap.of("Field1", "User Value"));
        List<DocumentServerMetadataDTO> metadataDtos =
                Arrays.asList(
                        new DocumentServerMetadataDTO("123", "Field1", "DataSource Value"),
                        new DocumentServerMetadataDTO("456", "Field2", null));
        // act
        DocumentIndexingDTO result = mapper.buildIndexingDtoFromServerMetadataDtos(metadataDtos);

        // assert
        assertThat(
                result.getDocumentIndexingMetadataDtos(),
                is(Arrays.asList(new DocumentIndexingMetadataDTO("123", "DataSource Value"))));
    }

    /**
    * Note the difference in how DocFinity treats multi-selection between the two DTO's.
    * DocumentServerMetadataDTO has an array of values on a single metadata object and
    * DocumentIndexingDTO has multiple metadata objects for each value. The former is the response
    * from the '/indexing/controls' call and the latter is the request to the
    * '/indexing/index/commit' call.
    */
    @Test
    public void buildIndexingDtoShouldExpandMultiSelectValuesFromServerMetadata() {
        // arrange
        DocFinityDtoMapper mapper = buildMapper(ImmutableMap.of("Field1", "User Value"));
        DocumentServerMetadataDTO metadataDto = new DocumentServerMetadataDTO("123", "Test Field");
        metadataDto.setStrDefaultValue(new String[] {"Value1", "Value2"});

        // act
        DocumentIndexingDTO result =
                mapper.buildIndexingDtoFromServerMetadataDtos(Arrays.asList(metadataDto));

        // assert
        assertThat(
                result.getDocumentIndexingMetadataDtos(),
                is(
                        Arrays.asList(
                                new DocumentIndexingMetadataDTO("123", "Value1"),
                                new DocumentIndexingMetadataDTO("123", "Value2"))));
    }
}
