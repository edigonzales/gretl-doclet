# gretl-doclet

Erstellt aus den Gretl-Custom-Tasks Markdowndateien mit folgendem Inhalt:

```
Parameter | Datentyp | Beschreibung | Optional
----------|----------|-------------|-------------
afield | `double` | null | ja
aMapList | `List<Map<String,Double>>` | null | nein
empty | `List<String>` | null | nein
foo | `String` | Im A Comment | ja
fubar | `double` | Dazugehörige Methode | ja
```


## Usage
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

## Issues
- `-subpackages` funktioniert nicht. Es müssen explizit Klassen ausgewählt werden.
- Das Herstellen der unqualifierten Typen ist wahrscheinlich nicht enorm robust. Wird sich zeigen.
- Vielleicht muss ein Minimum an HTML nach Markdown unterstützt werden. <pre> z.B.?
