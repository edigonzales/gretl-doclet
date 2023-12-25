package ch.so.agi.gretl.doclet.test;

public class DummyTask extends DummySuperTask {
    /**
     * Im A Comment
     */
    @Input
    public String foo;
    
    /**
     * private field
     */
    private double fubar;
    
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
    
}
