package ch.so.agi.gretl.doclet;

import java.io.File;
import java.util.Arrays;

import javax.tools.DocumentationTool;
import javax.tools.ToolProvider;

import org.junit.jupiter.api.Test;

public class GretlDocletTest {
    @Test 
    public void dummy() {
        String classpath = System.getProperty("java.class.path");

        DocumentationTool systemDocumentationTool = ToolProvider.getSystemDocumentationTool();
        String[] args = new String[] {
//          "-sourcepath",
//          "./src/test/resource",
//          "-subpackages",
//          "<whatever-package-in-test-sources>",
//          "<whatever-package-in-test-sources>",
          //"-d",
          //"whatever not used just to show compatibility",
          //"--show-elements",
          //"--show-comments",
          "--output", "/Users/stefan/tmp/fubar.html",      
          "-classpath",
          classpath,
          "--source-path", "./src/test/java/",
          "-subpackages", "ch.so.agi.gretl.doclet.test"
          //"./src/test/resources/DummyTask.java"
        };
        DocumentationTool.DocumentationTask task = systemDocumentationTool.getTask(null, null, null, 
                GretlDoclet.class, Arrays.asList(args), null);

        task.call();

    }
}
