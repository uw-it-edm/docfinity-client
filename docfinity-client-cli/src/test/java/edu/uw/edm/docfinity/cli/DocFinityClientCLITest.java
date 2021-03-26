package edu.uw.edm.docfinity.cli;

import org.junit.Test;
import static org.junit.Assert.*;

public class DocFinityClientCLITest {
    @Test public void testPlayground() {
        DocFinityClientCLI cli = new DocFinityClientCLI();
        assertTrue("CLI should return 'true'", cli.run(new String[0]));
    }
}
