package edu.uw.edm.docfinity;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.uw.edm.docfinity.models.DocumentTypesResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.Response;

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

    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json");
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
                            .header("Authorization", "Bearer " + apiKey)
                            .header("X-XSRF-TOKEN", "edm-token")
                            .header("Cookie", "XSRF-TOKEN=edm-token");

            if (auditUser != null && !auditUser.trim().isEmpty()) {
                // Audit user is not required and only needs to be added if supplied.
                builder = builder.header("X-AUDITUSER", auditUser);
            }

            logRequest(originalRequest);

            Response response = chain.proceed(builder.build());

            logResponse(response);

            return response;
        }
    }

    @Override
    public DocumentTypesResponse getDocumentTypes(String categoryName, String documentTypeName)
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

            return objectMapper.readValue(response.body().string(), DocumentTypesResponse.class);
        }
    }

    private void logRequest(Request request) {
        log.trace("[Request] {}  {}", request.method(), request.url());
    }

    private void logResponse(Response response) {
        log.trace("[Response] Status Code: {}", response.code());
    }
}
