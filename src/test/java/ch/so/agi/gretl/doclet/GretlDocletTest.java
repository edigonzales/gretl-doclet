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
          "-d", "/Users/stefan/tmp/markdown/",      
          "-classpath",
          classpath,
          //"--source-path", "./src/test/java/",
          //"-subpackages", "ch.so.agi.gretl.doclet.test"
          "./src/test/java/ch/so/agi/gretl/doclet/test/DummyTask.java"
        };
        DocumentationTool.DocumentationTask task = systemDocumentationTool.getTask(null, null, null, 
                GretlDoclet.class, Arrays.asList(args), null);

        task.call();

    }
}
