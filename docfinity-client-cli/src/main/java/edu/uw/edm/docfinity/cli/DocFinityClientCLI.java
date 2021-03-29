package edu.uw.edm.docfinity.cli;

import ch.qos.logback.classic.Logger;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import edu.uw.edm.docfinity.CreateDocumentResult;
import edu.uw.edm.docfinity.DocFinityClient;
import java.io.IOException;
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

    @Parameter(names = "--trace", description = "Enable request tracing to console.")
    boolean trace;

    @Parameter(
            names = "--auditUser",
            description = "Username of DocFinity account to use for auditing when sending requests.")
    String auditUser;

    @Parameter(names = "--help", help = true)
    private boolean help = false;

    public static void main(String... argv) throws IOException {
        Logger cliLogger = (Logger) LoggerFactory.getLogger(DocFinityClientCLI.class);

        DocFinityClientCLI cli = new DocFinityClientCLI();

        JCommander jCommander = JCommander.newBuilder().addObject(cli).build();
        jCommander.parse(argv);

        if (cli.help) {
            jCommander.usage();
            return;
        }

        if (!cli.trace) {
            // A console appender is defined in 'docfinity-client-cli/src/main/resources/logback.xml', if
            // user did not enable tracing need to detach it.
            Logger clientLogger = (Logger) LoggerFactory.getLogger("edu.uw.edm.docfinity");
            clientLogger.detachAppender("Console");
        }

        cliLogger.info("Starting");
        DocFinityClient client = new DocFinityClient(cli.url, cli.apiKey, cli.auditUser);
        CreateDocumentResult result = client.createDocument(null, cli.category, cli.documentType, null);
        cliLogger.info("Result: {}", result.getDocumentTypeId());
    }
}
