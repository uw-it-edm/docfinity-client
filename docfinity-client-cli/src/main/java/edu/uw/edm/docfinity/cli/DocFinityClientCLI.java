package edu.uw.edm.docfinity.cli;

import ch.qos.logback.classic.Logger;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import edu.uw.edm.docfinity.CreateDocumentArgs;
import edu.uw.edm.docfinity.CreateDocumentResult;
import edu.uw.edm.docfinity.DocFinityClient;
import edu.uw.edm.docfinity.DocFinityDocumentField;
import edu.uw.edm.docfinity.DocFinityServiceImpl;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import org.slf4j.LoggerFactory;

/**
* The entry point of command line runner that can parse arguments and invoke the DocFinity client.
*/
public class DocFinityClientCLI {
    @Parameter(
            names = {"--url", "-u"},
            required = true,
            description = "DocFinity base url.")
    String url;

    @Parameter(
            names = {"--key", "-k"},
            required = true,
            description = "User API Key to use when interacting with DocFinity REST API.")
    String apiKey;

    @Parameter(
            names = {"--category", "-c"},
            required = true,
            description = "Name of document category to use when creating a new document.")
    String category;

    @Parameter(
            names = {"--documentType", "-d"},
            required = true,
            description = "Name of document type to use when creating a new document.")
    String documentType;

    @Parameter(
            names = {"--file", "-f"},
            required = true,
            description = "Path of file to upload, or 'test' to upload a sample file")
    String filePath;

    @Parameter(
            names = {"--metadataJson", "-j"},
            description = "Json array with metadata values to index the file.")
    String metadataJson = "[]";

    @Parameter(
            names = {"--metadataFile", "-m"},
            description = "File path with with metadata values as Json to use when indexing.")
    String metadataFilePath;

    @Parameter(names = "--trace", description = "Enable request tracing to console.")
    boolean trace;

    @Parameter(
            names = "--auditUser",
            description = "Username of DocFinity account to use for auditing when sending requests.")
    String auditUser;

    @Parameter(names = "--help", help = true)
    private boolean help = false;

    public static void main(String... argv) throws Exception {
        Logger cliLogger = (Logger) LoggerFactory.getLogger(DocFinityClientCLI.class);

        DocFinityClientCLI cli = new DocFinityClientCLI();

        JCommander jCommander = JCommander.newBuilder().addObject(cli).build();
        jCommander.parse(argv);

        if (cli.help) {
            jCommander.usage();
            return;
        }

        // Load the file to upload.
        File file = getFile(cli);

        // Load metadata from json
        List<DocFinityDocumentField> metadata = loadMetadata(cli);

        // Setup logging for request/responses.
        setupRequestTracing(cli);

        // Run the client.
        cliLogger.info("Starting");
        DocFinityClient client = new DocFinityClient(cli.url, cli.apiKey, cli.auditUser);
        CreateDocumentArgs args =
                new CreateDocumentArgs(file, cli.category, cli.documentType).withMetadata(metadata);
        CreateDocumentResult result = client.createDocument(args);
        cliLogger.info("Result: {}", result.getDocumentId());
    }

    private static void setupRequestTracing(DocFinityClientCLI cli) {
        if (!cli.trace) {
            // A console appender is defined in 'docfinity-client-cli/src/main/resources/logback.xml', if
            // user did not enable tracing need to detach it.
            String serviceImplTypeName = DocFinityServiceImpl.class.getCanonicalName();
            Logger clientLogger = (Logger) LoggerFactory.getLogger(serviceImplTypeName);
            clientLogger.detachAppender("Console");
        }
    }

    private static File getFile(DocFinityClientCLI cli) throws Exception {
        Preconditions.checkNotNull(cli.filePath, "file is required.");
        File file = new File(cli.filePath);

        if (cli.filePath.equals("test")) {
            URL resource = DocFinityClientCLI.class.getClassLoader().getResource("test-file.pdf");
            file = new File(resource.toURI());
        }
        return file;
    }

    private static List<DocFinityDocumentField> loadMetadata(DocFinityClientCLI cli)
            throws Exception {
        String metadataJson = cli.metadataJson;
        if (cli.metadataFilePath != null) {
            metadataJson = new String(Files.readAllBytes(Paths.get(cli.metadataFilePath)));
        }

        ObjectMapper mapper = new ObjectMapper();
        return Arrays.asList(mapper.readValue(metadataJson, DocFinityDocumentField[].class));
    }
}
