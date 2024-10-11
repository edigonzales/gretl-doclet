package ch.so.agi.gretl.doclet;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import javax.tools.DocumentationTool;
import javax.tools.ToolProvider;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class GretlDocletTest {

    private String classpath = System.getProperty("java.class.path");

    private DocumentationTool systemDocumentationTool = ToolProvider.getSystemDocumentationTool();

    @Test 
    public void vintageTask(@TempDir Path tempDir) throws IOException {
        // Create documentation
        String[] args = new String[] {
          "-d", tempDir.toString(),      
          "-classpath",
          classpath,
          "./src/test/java/ch/so/agi/gretl/doclet/test/DummyTask.java"
        };
        DocumentationTool.DocumentationTask task = systemDocumentationTool.getTask(null, null, null, GretlDoclet.class,
                Arrays.asList(args), null);

        task.call();

        // Validation result
        Path path = tempDir.resolve("_ch.so.agi.gretl.doclet.test.DummyTask.md");
        String content = Files.readString(path);
        
        assertTrue(content.contains("afield | `double` | null | ja"));
        assertTrue(content.contains("bloedeMapList | `List<Map<String,Double>>` | null | nein"));
        assertTrue(content.contains("empty | `List<String>` | null | nein"));
        assertTrue(content.contains("foo | `String` | Im A Comment | ja"));
        assertTrue(content.contains("fubar | `double` | Dazugehörige Methode | ja"));
    }
    
    @Test 
    public void nonManagedPropertiesTask(@TempDir Path tempDir) throws IOException {
        // Create documentation
        String[] args = new String[] {
          "-d", tempDir.toString(),      
          "-classpath",
          classpath,
          "./src/test/java/ch/so/agi/gretl/doclet/test/NonManagedPropertyTask.java"
        };
        DocumentationTool.DocumentationTask task = systemDocumentationTool.getTask(null, null, null, GretlDoclet.class,
                Arrays.asList(args), null);

        task.call();

        // Validation result
        Path path = tempDir.resolve("_ch.so.agi.gretl.doclet.test.NonManagedPropertyTask.md");
        String content = Files.readString(path);
        
        assertTrue(content.contains("outDirectory | `File` | null | nein"));
        assertTrue(content.contains("xmlFile | `Object` | null | nein"));
        assertTrue(content.contains("xslFile | `Object` | null | nein"));
    }
    
    @Test 
    public void managedPropertiesTask(@TempDir Path tempDir) throws IOException {
        // Create documentation
        String[] args = new String[] {
          "-d", tempDir.toString(),      
          "-classpath",
          classpath,
          "./src/test/java/ch/so/agi/gretl/doclet/test/ManagedPropertyTask.java"
        };
        DocumentationTool.DocumentationTask task = systemDocumentationTool.getTask(null, null, null, GretlDoclet.class,
                Arrays.asList(args), null);

        task.call();

        // Validation result
        Path path = tempDir.resolve("_ch.so.agi.gretl.doclet.test.ManagedPropertyTask.md");
        String content = Files.readString(path);
        
        assertTrue(content.contains("database | `ListProperty<String>` | null | nein"));
        assertTrue(content.contains("sqlFiles | `ListProperty<String>` | null | nein"));
        assertTrue(content.contains("sqlParameters | `Property<Object>` | null | ja"));
    }
}
