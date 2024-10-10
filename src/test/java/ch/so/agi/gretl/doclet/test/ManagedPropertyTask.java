package ch.so.agi.gretl.doclet.test;

import org.gradle.api.DefaultTask;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

//@GretlClass
public abstract class ManagedPropertyTask extends DefaultTask {

//    @GretlDesc()
    @Input
    public abstract ListProperty<String> getDatabase();

    @Input
    public abstract ListProperty<String> getSqlFiles();
    
    @Input
    @Optional
    public abstract Property<Object> getSqlParameters();

    @TaskAction
    public void executeSQLExecutor() {
        // do nothing
    }
}
