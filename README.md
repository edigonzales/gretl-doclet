# gretl-doclet

- `-subpackages` funktioniert nicht. Es müssen explizit Klassen ausgewählt werden.
- Das Herstellen der unqualifierten Typen ist wahrscheinlich nicht enorm robust. Wird sich zeigen.
- Die grösste Herausforderung wird wohl sein, wenn wir vermehrt mit Lazy Properties und Managed Types arbeiten. Dann könne nicht nur die Fields geparsed werden, sondern auch Methoden. Anschliessend ggf. abgleichen mit Fields. Aber vielleicht auch nicht super tragisch, wenn man klar ist, was wir wollen.
- Das Doclet muss noch irgendwo hin deployed werden. Jar auf Github würde eigentlich reichen. 
- Version braucht das Jar noch.
- Test muss noch asserten.
- Vielleicht muss ein Minimum an HTML nach Markdown unterstützt werden. <pre> z.B.?

```
javadoc {
    source = sourceSets.main.allJava

    title = null
    destinationDir = file("./doc/")
    
    include 'ch/so/agi/gretl/tasks/**'
    options.doclet = "ch.so.agi.gretl.doclet.GretlDoclet"
    options.docletpath = [file("/Users/stefan/sources/gretl-doclet/build/libs/gretl-doclet.jar")]    
}
```