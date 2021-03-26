package edu.uw.edm.docfinity.cli;

import edu.uw.edm.docfinity.DocFinityClient;

public class DocFinityClientCLI {
    public static void main(String ... argv) {
        DocFinityClientCLI cli = new DocFinityClientCLI();
        cli.run(argv);
    }

    public boolean run(String ... argv) {
        DocFinityClient client = new DocFinityClient();
        boolean result = client.someLibraryMethod();
        System.out.printf("CLI Running. Result is %b", result);
        return result;
    }
}
