package ch.so.agi.gretl.doclet.test;

import java.io.File;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.TaskAction;

public class NonManagedPropertyTask extends DefaultTask {
    private Object xslFile;
    private Object xmlFile;
    private File outDirectory;

    @Input
    public Object getXslFile() {
        return xslFile;
    }
    
    @Input
    public Object getXmlFile() {
        return xmlFile;
    }
    
    @InputDirectory
    public File getOutDirectory() {
        return outDirectory;
    }
    
    public void setXslFile(Object xslFile) {
        this.xslFile = xslFile;
    }

    public void setXmlFile(Object xmlFile) {
        this.xmlFile = xmlFile;
    }

    public void setOutDirectory(File outDirectory) {
        this.outDirectory = outDirectory;
    }

    @TaskAction
    public void transform() {
        // do nothing
    }
}
