package ch.so.agi.gretl.doclet.test;

import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;

public abstract class DummySuperSuperTask {
    @Input
    @Optional
    public double afield;
}
