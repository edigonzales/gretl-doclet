package ch.so.agi.gretl.doclet.test;

import java.util.List;
import java.util.Map;

import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

public class DummyTask extends DummySuperTask {
    /**
     * Im A Comment
     */
    @Input
    @Optional
    public String foo;
    
    /**
     * private field
     */
    private double fubar;
    
    @Input
    public List<String> empty;

    @Input
    public List<Map<String,Double>> bloedeMapList;

    /**
     * Im a Description
     * 
     * @return gaga
     */
    public String bar() {
        return "bar";
    }
    
    
    /**
     * Dazugehörige Methode
     */
    @Input
    @Optional
    public double getFubar() {
        return fubar;
    }
    
    @TaskAction
    public void run() {
        
    }
}
