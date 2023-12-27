package ch.so.agi.gretl.doclet;

import java.io.PrintStream;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Stack;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.ElementScanner9;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
 
import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.util.DocTreeScanner;
import com.sun.source.util.DocTrees;
import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;
 

import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.DeclaredType;
import javax.tools.Diagnostic;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * A doclet to demonstrate the use of {@link ElementScanner9}
 * and {@link DocTreeScanner}.
 *
 * @version 1.0
 * @author Duke
 */
public class GretlDoclet implements Doclet {
    private String outputDirectory;
    
    private DocTrees dcTreeUtils;
    private Elements elementsUtils;
    
    private static final List<String> GRADLE_ANNOTATIONS = new ArrayList<>() {{
        add("Input");
        add("InputFile");
        add("Optional");
        add("OutputFile");
        add("OutputDirectory");
    }};
    
    private static final String TASK_ACTION_ANNOTATION = "TaskAction";
    private static final String OPTIONAL_ANNOTATION = "Optional";

    @Override
    public void init(Locale locale, Reporter reporter) {}
 
    @Override
    public String getName() {
        return getClass().getSimpleName();
    }
 
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }
 
    @Override
    public boolean run(DocletEnvironment env) {
        
        File outputDir = new File(outputDirectory);
        if(!outputDir.exists())
            outputDir.mkdirs();
        
        // Alle Klassen suchen. Die abstrakten Klassen werden ignoriert und ihre
        // Attribute (Parameter) müssen bei der Kindklasse eruiert werden.
        // ACHTUNG: In der neuen Syntax sind alle Task-Klassen abstract.
        // Vielleicht nach einer Methode Ausschau halten, mit der TaskAction-
        // Annotation? (preprocessing)
        // Oder auch nicht. Müssen wir dann entscheiden. Abstrakt glaub nur, falls
        // es managed types sind. Das ist nicht zwingend für lazy. Aber hat den 
        // Vorteil, dass man den Getter nicht schreiben muss. (?)
        List<Element> clazzes = getSpecifiedClasses(env).collect(Collectors.toList());
        Iterator<? extends Element> classesIterator = clazzes.iterator();
            
            
        // Beschreibungen (gemäss Spez) sämtlicher Element.
        dcTreeUtils = env.getDocTrees();
        
        // Annotationen (und andere Infos) sämtlicher Elemente.
        elementsUtils = env.getElementUtils();

        while (classesIterator.hasNext()) {
            Element cls = classesIterator.next();                                                
            System.out.println("className: " + cls);
            
            File outFile = Paths.get(outputDir.getAbsolutePath(), cls.toString() + ".md").toFile();
            try (FileWriter fw = new FileWriter(outFile); PrintWriter pw = new PrintWriter(fw, true)) {
                pw.println("Parameter | Datentyp | Beschreibung | Optional");
                pw.println("----------|----------|-------------|-------------");
                
                // Nur Klassen mit einer TaskAction-annotierten Methoden werden behandelt.
                if (!findTaskAction(cls)) 
                    continue;
                
                TypeElement classElement = (TypeElement) cls;
                
                List<Property> properties = new ArrayList<>();
                getProperties(classElement, properties); 
                
                properties.sort(Comparator.comparing(Property::getName));

                for (Property property : properties) {
                    pw.println(property.getName() + " | `" + property.getType() + "` | " + property.getDescription() + " | " + (property.isMandatory() ? "nein" : "ja"));         
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return true;
    }
    
    private void getProperties(Element cls, List<Property> properties) {
        for (Element element : cls.getEnclosedElements()) {
            if (element.getKind().isField()) {                
                boolean isGradleProperty = elementsUtils.getAllAnnotationMirrors(element).stream().map(annot -> {
                    String annotSimpleName = annot.toString().substring(annot.toString().lastIndexOf(".")+1);
                    return annotSimpleName;
                })
                .anyMatch(GRADLE_ANNOTATIONS::contains);
                
                if (!isGradleProperty) 
                    continue;
                
                boolean isOptional = elementsUtils.getAllAnnotationMirrors(element).stream().map(annot -> {
                    String annotSimpleName = annot.toString().substring(annot.toString().lastIndexOf(".")+1);
                    return annotSimpleName;
                })
                .filter(annot -> {
                    if(annot.contains(OPTIONAL_ANNOTATION)) {
                        return true;
                    }
                    return false;
                }).count() > 0; 

                Property property = new Property();
                property.setName(element.getSimpleName().toString());
                if (dcTreeUtils.getDocCommentTree(element) != null) {
                    property.setDescription(dcTreeUtils.getDocCommentTree(element).toString());
                }
                String qualifiedFieldType = element.asType().toString();
                property.setQualifiedType(qualifiedFieldType);
                String unqualifiedFieldType = getUnqualifiedFieldType(qualifiedFieldType);
                property.setType(unqualifiedFieldType);
                property.setMandatory(!isOptional);
                
                properties.add(property);
            }
        }
        
        TypeElement classElement = (TypeElement) cls;        
        if (classElement.getSuperclass() != null) {
            Element scls = elementsUtils.getTypeElement(classElement.getSuperclass().toString());
            if (scls.getSimpleName().toString().equalsIgnoreCase("Object")) {
                return;
            }
            getProperties(scls, properties);
        } else {
            return;
        }
    }
    
    private String getUnqualifiedFieldType(String fieldType) {        
        if (!fieldType.contains(".")) {
            return fieldType;
        }
        
        if (!fieldType.contains("<")) {
            return fieldType.substring(fieldType.lastIndexOf(".") + 1);
        }
        
        StringBuilder unqualifiedFieldType = new StringBuilder();
        
        String[] partsBracket = fieldType.split("[<]");
        for (int i=0; i<partsBracket.length; i++) {
            if (i>0) {
                unqualifiedFieldType.append("<");
            }
            String[] partsComma = partsBracket[i].split(",");
            for(int ii=0; ii<partsComma.length; ii++) {
                unqualifiedFieldType.append(getUnqualifiedFieldType(partsComma[ii]));
                if (ii<partsComma.length-1) {
                    unqualifiedFieldType.append(",");
                }
            }            
        }
        return unqualifiedFieldType.toString();
    }
    
    private boolean findTaskAction(Element classElement) {
        for (Element element : classElement.getEnclosedElements()) {
            for (AnnotationMirror annot : elementsUtils.getAllAnnotationMirrors(element)) {
                if (annot.toString().endsWith(TASK_ACTION_ANNOTATION)) {
                    return true;
                }
            }
        }
        return false;
    }
            
    @Override
    public Set<? extends Option> getSupportedOptions() {        
        return options;
    }

    private Stream<TypeElement> getSpecifiedClasses(DocletEnvironment root) {
        return root.getSpecifiedElements()
                .stream()
                .filter(element -> {                    
                    if (ElementKind.CLASS == element.getKind()) {
                        return true;
                    }
                    return false;
                    })
//                .filter(element -> env.isSelected(element) && env.isIncluded(element))
                .filter(element -> element.getModifiers().contains(Modifier.PUBLIC))
                .filter(element -> !element.getModifiers().contains(Modifier.ABSTRACT))
                .sorted(Comparator.comparing((Element o) -> o.getSimpleName()
                        .toString()))                
                .map(element -> (TypeElement) element);
    }
        
    private Stream<PackageElement> getSpecifiedPackages(DocletEnvironment root) {
        return root.getSpecifiedElements()
                .stream()
                .filter(element -> {                    
                    if (ElementKind.PACKAGE == element.getKind()) {
                        return true;
                    }
                    //System.out.println("element: " + element);
                    return false;
                    })
                .map(element -> (PackageElement) element);
    }
    
    private final Set<Option> options = Set.of( 
            new Option("-d", true, "Output directory path", "<string>") {
                @Override
                public boolean process(String option,
                                       List<String> arguments) {
                    outputDirectory = arguments.get(0);                    
                    return true;
                }
            } 
    );
    
    abstract class Option implements Doclet.Option {
        private final String name;
        private final boolean hasArg;
        private final String description;
        private final String parameters;
 
        Option(String name, boolean hasArg,
               String description, String parameters) {
            this.name = name;
            this.hasArg = hasArg;
            this.description = description;
            this.parameters = parameters;
        }
 
        @Override
        public int getArgumentCount() {
            return hasArg ? 1 : 0;
        }
 
        @Override
        public String getDescription() {
            return description;
        }
 
        @Override
        public Kind getKind() {
            return Kind.STANDARD;
        }
 
        @Override
        public List<String> getNames() {
            return List.of(name);
        }
 
        @Override
        public String getParameters() {
            return hasArg ? parameters : "";
        }
    }
}
