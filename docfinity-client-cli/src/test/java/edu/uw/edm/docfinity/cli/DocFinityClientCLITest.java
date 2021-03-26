package edu.uw.edm.docfinity.cli;

import static org.junit.Assert.*;

import org.junit.Test;

public class DocFinityClientCLITest {
    @Test
    public void testPlayground() {
        DocFinityClientCLI cli = new DocFinityClientCLI();
        assertTrue("CLI should return 'true'", cli.run(new String[0]));
    }
}
