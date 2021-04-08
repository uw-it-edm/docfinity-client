package edu.uw.edm.docfinity;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.junit.Assert.assertThrows;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import edu.uw.edm.docfinity.models.DocumentIndexingMetadataDTO;
import edu.uw.edm.docfinity.models.DocumentServerMetadataDTO;
import edu.uw.edm.docfinity.models.DocumentTypeMetadataDTO;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class DocFinityDtoMapperTest {
    private static final String documentTypeName = "documentType";
    private static final String categoryName = "category";

    private DocFinityDtoMapper buildMapper(Map<String, Object> userMetadata) {
        CreateDocumentArgs args =
                new CreateDocumentArgs(categoryName, documentTypeName).withMetadata(userMetadata);
        return new DocFinityDtoMapper(args);
    }

    private DocFinityDtoMapper buildMapper(Multimap<String, Object> userMetadata) {
        CreateDocumentArgs args = new CreateDocumentArgs(categoryName, documentTypeName);
        args.setMetadata(userMetadata);
        return new DocFinityDtoMapper(args);
    }

    @Test
    public void getPartialIndexingDtoShouldIncludeMultiSelectValues() {
        // arrange
        Multimap<String, Object> userValues = ArrayListMultimap.create();
        userValues.put("Field", "Value1");
        userValues.put("Field", "Value2");
        DocFinityDtoMapper mapper = buildMapper(userValues);
        ImmutableMap<String, DocumentTypeMetadataDTO> metadataMap =
                ImmutableMap.of("Field", new DocumentTypeMetadataDTO("123", "Field"));

        // act
        List<DocumentIndexingMetadataDTO> result = mapper.getPartialIndexingDtos(metadataMap);

        // assert
        assertThat(
                result,
                is(
                        Arrays.asList(
                                new DocumentIndexingMetadataDTO("123", "Field", "Value1"),
                                new DocumentIndexingMetadataDTO("123", "Field", "Value2"))));
    }

    @Test
    public void getPartialIndexingDtoShouldThrowErrorIfMetadataIsMissing() {
        // arrange
        DocFinityDtoMapper mapper = buildMapper(ImmutableMap.of("Field3", "User Value"));
        DocumentTypeMetadataDTO metadata1 = new DocumentTypeMetadataDTO("111", "Field1");
        DocumentTypeMetadataDTO metadata2 = new DocumentTypeMetadataDTO("222", "Field2");
        Map<String, DocumentTypeMetadataDTO> metadata =
                ImmutableMap.of("Field1", metadata1, "Field2", metadata2);

        // act
        IllegalStateException thrown =
                assertThrows(IllegalStateException.class, () -> mapper.getPartialIndexingDtos(metadata));

        // assert
        assertThat(
                thrown.getMessage(),
                containsString("Document type 'documentType' is missing metadata object named 'Field3'."));
    }

    @Test
    public void getPartialIndexingDtoShouldThrowErrorForInvalidIntegerMetadata() {
        // arrange
        DocFinityDtoMapper mapper = buildMapper(ImmutableMap.of("Field1", 100.10));
        DocumentTypeMetadataDTO field1 = new DocumentTypeMetadataDTO("111", "Field1");
        field1.setMetadataType(MetadataTypeEnum.INTEGER);
        Map<String, DocumentTypeMetadataDTO> metadata = ImmutableMap.of("Field1", field1);

        // act
        IllegalStateException thrown =
                assertThrows(IllegalStateException.class, () -> mapper.getPartialIndexingDtos(metadata));

        // assert
        assertThat(
                thrown.getMessage(), containsString("Invalid integer value for metadata object 'Field1'"));
    }

    @Test
    public void getFinalIndexingDtoShouldRemoveNullValuesFromServerMetadata() {
        // arrange
        DocFinityDtoMapper mapper = buildMapper(ImmutableMap.of("Field1", "User Value"));
        Map<String, DocumentTypeMetadataDTO> metadataMap =
                ImmutableMap.of("Field1", new DocumentTypeMetadataDTO("123", "Field1"));
        List<DocumentServerMetadataDTO> serverDtos =
                Arrays.asList(
                        new DocumentServerMetadataDTO("123", "Field1", "DataSource Value"),
                        new DocumentServerMetadataDTO("456", "Field2", null));
        // act
        List<DocumentIndexingMetadataDTO> result = mapper.getFinalIndexingDtos(metadataMap, serverDtos);

        // assert
        assertThat(
                result,
                is(Arrays.asList(new DocumentIndexingMetadataDTO("123", "Field1", "DataSource Value"))));
    }

    /**
    * Note the difference in how DocFinity treats multi-selection between the two DTO's.
    * DocumentServerMetadataDTO has an array of values on a single metadata object and
    * DocumentIndexingDTO has multiple metadata objects for each value. The former is the response
    * from the '/indexing/controls' call and the latter is the request to the
    * '/indexing/index/commit' call.
    */
    @Test
    public void getFinalIndexingDtoShouldExpandMultiSelectValuesFromServerMetadata() {
        // arrange
        DocFinityDtoMapper mapper = buildMapper(ImmutableMap.of("Field1", "User Value"));
        Map<String, DocumentTypeMetadataDTO> metadataMap =
                ImmutableMap.of("Field1", new DocumentTypeMetadataDTO("123", "Field1"));
        DocumentServerMetadataDTO serverDto = new DocumentServerMetadataDTO("123", "Field1");
        serverDto.setStrDefaultValue(new String[] {"Value1", "Value2"});

        // act
        List<DocumentIndexingMetadataDTO> result =
                mapper.getFinalIndexingDtos(metadataMap, Arrays.asList(serverDto));

        // assert
        assertThat(
                result,
                is(
                        Arrays.asList(
                                new DocumentIndexingMetadataDTO("123", "Field1", "Value1"),
                                new DocumentIndexingMetadataDTO("123", "Field1", "Value2"))));
    }

    @Test
    public void getFinalIndexingDtoShouldThrowErrorIfRequiredFieldIsNull() {
        // arrange
        DocFinityDtoMapper mapper = buildMapper(ImmutableMap.of());
        Map<String, DocumentTypeMetadataDTO> metadataMap =
                ImmutableMap.of("Field1", new DocumentTypeMetadataDTO("123", "Field1", true));
        List<DocumentServerMetadataDTO> serverDtos =
                Arrays.asList(new DocumentServerMetadataDTO("123", "Field1", null));

        // act
        IllegalStateException thrown =
                assertThrows(
                        IllegalStateException.class,
                        () -> mapper.getFinalIndexingDtos(metadataMap, serverDtos));

        // assert
        assertThat(thrown.getMessage(), containsString("Missing value for required metadata 'Field1'"));
    }

    @Test
    public void getFinalIndexingDtoShouldThrowErrorIfRequiredFieldIsEmptyString() {
        // arrange
        DocFinityDtoMapper mapper = buildMapper(ImmutableMap.of());
        Map<String, DocumentTypeMetadataDTO> metadataMap =
                ImmutableMap.of("Field1", new DocumentTypeMetadataDTO("123", "Field1", true));
        List<DocumentServerMetadataDTO> serverDtos =
                Arrays.asList(new DocumentServerMetadataDTO("123", "Field1", ""));

        // act
        IllegalStateException thrown =
                assertThrows(
                        IllegalStateException.class,
                        () -> mapper.getFinalIndexingDtos(metadataMap, serverDtos));

        // assert
        assertThat(thrown.getMessage(), containsString("Missing value for required metadata 'Field1'"));
    }
}
