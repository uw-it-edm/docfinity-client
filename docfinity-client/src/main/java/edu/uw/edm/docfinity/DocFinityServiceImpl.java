package edu.uw.edm.docfinity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import edu.uw.edm.docfinity.models.DatasourceRunningDTO;
import edu.uw.edm.docfinity.models.DocumentIndexingDTO;
import edu.uw.edm.docfinity.models.DocumentServerMetadataDTO;
import edu.uw.edm.docfinity.models.DocumentTypeDTOSearchResult;
import edu.uw.edm.docfinity.models.DocumentTypeMetadataDTO;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;

/**
* Implementation of DocFinityService that uses OkHttp client (https://square.github.io/okhttp/) to
* interact with DocFinity REST API.
*/
@Slf4j
public class DocFinityServiceImpl implements DocFinityService {
    private final OkHttpClient client;
    private final HttpUrl docFinityUrl;
    private final String apiKey;
    private final String auditUser;

    private static final String HEADER_XSRF_TOKEN = "X-XSRF-TOKEN";
    private static final String HEADER_XSRF_TOKEN_VALUE = "edm-token";
    private static final String HEADER_AUDIT_USER = "X-AUDITUSER";
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String HEADER_COOKIE = "Cookie";
    private static final String HEADER_COOKIE_VALUE = "XSRF-TOKEN=edm-token";
    private static final String LOG_NO_REQUEST_BODY = "[No Request Body]";
    private static final String LOG_NO_RESPONSE_BODY = "[No Response Body]";

    private static final MediaType MEDIA_TYPE_JSON =
            MediaType.parse("application/json; charset=utf-8");
    private static final MediaType MEDIA_TYPE_OCTET_STREAM =
            MediaType.parse("application/octet-stream");
    private static final String DOCUMENT_TYPES_FILTER_FORMAT_STRING =
            "{\"logic\": \"AND\",\"filters\": [{\"field\": \"name\",\"operator\": \"eq\",\"value\": \"%s\"},{\"field\": \"categoryName\",\"operator\": \"eq\",\"value\": \"%s\"}]}";

    public DocFinityServiceImpl(String url, String apikey, String auditUser) {
        this.docFinityUrl = HttpUrl.parse(url);
        this.apiKey = apikey;
        this.auditUser = auditUser;
        this.client = new OkHttpClient.Builder().addInterceptor(new ApiInterceptor()).build();
    }

    class ApiInterceptor implements Interceptor {
        @Override
        public Response intercept(Interceptor.Chain chain) throws IOException {
            Request originalRequest = chain.request();

            Builder builder =
                    originalRequest
                            .newBuilder()
                            .header(HEADER_AUTHORIZATION, "Bearer " + apiKey)
                            .header(HEADER_XSRF_TOKEN, HEADER_XSRF_TOKEN_VALUE)
                            .header(HEADER_COOKIE, HEADER_COOKIE_VALUE);

            if (!Strings.isNullOrEmpty(auditUser)) {
                // Audit user is not required and only needs to be added if supplied.
                builder = builder.header(HEADER_AUDIT_USER, auditUser);
            }

            logRequest(originalRequest);

            Response response = chain.proceed(builder.build());

            logResponse(response);

            return response;
        }
    }

    @Override
    public DocumentTypeDTOSearchResult getDocumentTypes(String categoryName, String documentTypeName)
            throws IOException {

        String filterExpression =
                String.format(DOCUMENT_TYPES_FILTER_FORMAT_STRING, documentTypeName, categoryName);
        HttpUrl requestUrl =
                this.docFinityUrl
                        .newBuilder()
                        .addPathSegments("webservices/rest/documentType")
                        .addQueryParameter("filter", filterExpression)
                        .addQueryParameter("includeNested", "false")
                        .build();

        Request request = new Request.Builder().url(requestUrl).build();

        try (Response response = client.newCall(request).execute()) {
            ObjectMapper objectMapper = new ObjectMapper();

            return objectMapper.readValue(response.body().string(), DocumentTypeDTOSearchResult.class);
        }
    }

    @Override
    public String uploadDocument(File file) throws IOException {
        return uploadDocument(file.getName(), RequestBody.create(file, MEDIA_TYPE_OCTET_STREAM));
    }

    @Override
    public String uploadDocument(byte[] content, String name) throws IOException {
        return uploadDocument(name, RequestBody.create(content, MEDIA_TYPE_OCTET_STREAM));
    }

    private String uploadDocument(String name, RequestBody fileRequestBody) throws IOException {
        HttpUrl requestUrl = this.docFinityUrl.newBuilder().addPathSegments("servlet/upload").build();

        RequestBody body =
                new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("json", "1")
                        .addFormDataPart("entryMethod", "FILE_UPLOAD")
                        .addFormDataPart("upload_files", name, fileRequestBody)
                        .build();

        Request request = new Request.Builder().url(requestUrl).post(body).build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    @Override
    public List<DocumentTypeMetadataDTO> getDocumentTypeMetadata(String documentTypeId)
            throws IOException {

        HttpUrl requestUrl =
                this.docFinityUrl
                        .newBuilder()
                        .addPathSegments("webservices/rest/documentType/metadata")
                        .addQueryParameter("id", documentTypeId)
                        .addQueryParameter("includeIndexing", "true")
                        .build();

        Request request = new Request.Builder().url(requestUrl).build();

        try (Response response = client.newCall(request).execute()) {
            ObjectMapper objectMapper = new ObjectMapper();

            return Arrays.asList(
                    objectMapper.readValue(response.body().string(), DocumentTypeMetadataDTO[].class));
        }
    }

    @Override
    public List<DocumentServerMetadataDTO> runDatasources(DatasourceRunningDTO datasourceRunningDto)
            throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        String requestJson = mapper.writeValueAsString(datasourceRunningDto);
        HttpUrl requestUrl =
                this.docFinityUrl
                        .newBuilder()
                        .addPathSegments("webservices/rest/indexing/controls")
                        .build();

        Request request =
                new Request.Builder()
                        .url(requestUrl)
                        .post(RequestBody.create(requestJson, MEDIA_TYPE_JSON))
                        .build();

        try (Response response = client.newCall(request).execute()) {
            return Arrays.asList(
                    mapper.readValue(response.body().string(), DocumentServerMetadataDTO[].class));
        }
    }

    @Override
    public List<DocumentIndexingDTO> indexDocuments(DocumentIndexingDTO... documents)
            throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String requestJson = mapper.writeValueAsString(documents);
        HttpUrl requestUrl =
                this.docFinityUrl
                        .newBuilder()
                        .addPathSegments("webservices/rest/indexing/index/commit")
                        .build();

        Request request =
                new Request.Builder()
                        .url(requestUrl)
                        .post(RequestBody.create(requestJson, MEDIA_TYPE_JSON))
                        .build();

        try (Response response = client.newCall(request).execute()) {
            return Arrays.asList(mapper.readValue(response.body().string(), DocumentIndexingDTO[].class));
        }
    }

    @Override
    public void deleteDocuments(String... documentIds) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String requestJson = mapper.writeValueAsString(documentIds);
        HttpUrl requestUrl =
                this.docFinityUrl.newBuilder().addPathSegments("webservices/rest/document/delete").build();

        Request request =
                new Request.Builder()
                        .url(requestUrl)
                        .post(RequestBody.create(requestJson, MEDIA_TYPE_JSON))
                        .build();

        try (Response response = client.newCall(request).execute()) {}
    }

    private void logRequest(Request request) throws IOException {
        String body = LOG_NO_REQUEST_BODY;
        if (request.body() != null && request.body().contentType().equals(MEDIA_TYPE_JSON)) {
            final Request copy = request.newBuilder().build();
            final Buffer buffer = new Buffer();
            copy.body().writeTo(buffer);
            body = buffer.readUtf8();
        }

        log.trace("[Request] {}  {}\n{}", request.method(), request.url(), body);
    }

    private void logResponse(Response response) throws IOException {
        String body = LOG_NO_RESPONSE_BODY;
        if (response.body() != null) {
            // Reading the response body will consume it directly from network and will be unavailable
            // by next consumer, work around is to peek as much body as possible in order to log it.
            ResponseBody responseBodyCopy = response.peekBody(Long.MAX_VALUE);
            body = responseBodyCopy.string();
        }

        log.trace("[Response] Status Code: {}\n{}\n", response.code(), body);
    }
}
