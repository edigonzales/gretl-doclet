package ch.so.agi.gretl.doclet;

import java.io.PrintStream;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
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
import javax.tools.Diagnostic;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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
    public static final String OPT_OUTFILE = "-outfile";
    private static final String JAVADOC_URL = "https://docs.oracle.com/en/java/javase/11/docs/api/";

    private Reporter reporter;
   
    private boolean alpha;
    private String outputFile;
    private int gamma;
    
    private DocTrees dcTreeUtils;
    private Elements elementsUtils;

    @Override
    public void init(Locale locale, Reporter reporter) {
        this.reporter = reporter;
    }
 
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
        try {

            File outFile = new File(outputFile);
            outFile.getParentFile().mkdirs();
            try (FileWriter fw = new FileWriter(outFile);
                 PrintWriter pw = new PrintWriter(fw, true)) {
                
                pw.println("<ol class=\"toc\" id=\"pageToc\">");
                getSpecifiedPackages(env)
                        .forEach(pack -> {
                            pw.format("  <li><a href=\"#Package_%s\">%s</a></li>\n",
                                    pack.getQualifiedName()
                                            .toString().replace('.', '_'),
                                    pack.getQualifiedName().toString());
                        });

                pw.println("</ol>\n\n");

                getSpecifiedPackages(env).forEach(pack -> {
                    pw.format("<h2 id=\"Package_%s\">Package %s</h2>\n",
                            pack.getQualifiedName().toString().replace('.', '_'),
                            pack.getQualifiedName().toString());
                    pw.println("<dl>");

                    String packURL = JAVADOC_URL + pack.getQualifiedName().toString()
                            .replace(".", "/") + "/";

                    // Alle Klassen suchen. Die abstrakten Klassen werden ignoriert und ihre
                    // Attribute (Parameter) müssen bei der Kindklasse eruiert werden.
                    // ACHTUNG: In der neuen Syntax sind alle Task-Klassen abstract.
                    // Vielleicht nach einer Methode Ausschau halten, mit der TaskAction-
                    // Annotation? (preprocessing)
                    // Oder auch nicht. Müssen wir dann entscheiden. Abstrakt glaub nur, falls
                    // es managed types sind. Das ist nicht zwingend für lazy. Aber hat den 
                    // Vorteil, dass man den Getter nicht schreiben muss. (?)
                    Iterator<? extends Element> classesIterator = pack.getEnclosedElements()
                            .stream()
                            .filter(element -> env.isSelected(element) && env.isIncluded(element))
                            .filter(element -> element.getModifiers().contains(Modifier.PUBLIC))
                            .filter(element -> !element.getModifiers().contains(Modifier.ABSTRACT))
                            .sorted(Comparator.comparing((Element o) -> o.getSimpleName()
                                    .toString()))
                            .iterator();
                    
                    DocTrees docTrees = env.getDocTrees();
                    System.out.println("classes:" + ElementFilter.typesIn(env.getIncludedElements()));

                    dcTreeUtils = env.getDocTrees();
                    System.out.println(dcTreeUtils);
                    
                    elementsUtils = env.getElementUtils();

                    while (classesIterator.hasNext()) {
                        Element cls = classesIterator.next();
                        System.out.println("className: " + cls.getSimpleName());
                        
                        TypeElement classElement = (TypeElement) cls;
                        System.out.println("super: " + classElement.getSuperclass());
                        
                        List<String> fields = new ArrayList<>();
                        getFields(classElement, fields); 
                        
                        System.out.println("fields: " + fields);
                        
//                        System.out.println(classElement.getModifiers().size());
//                        classElement.getModifiers().stream().filter(m -> {
//                            System.out.println(m.name());
//                            System.out.println("*****");
//                            return true;
//                        });
                        
                        // Each class links to Oracle's main JavaDoc
                        emitClassDocs(env, pw, packURL, cls);
                        if (classesIterator.hasNext()) {
                            pw.print("\n");
                        }
                    }

                    pw.println("</dl>\n");
                });
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return true;
    }
    
    private void getFields(TypeElement classElement, List<String> fields) {
        for (Element element : classElement.getEnclosedElements()) {
            System.out.println("element: " + element);
            System.out.println("kind: " + element.getKind());
            
            DocCommentTree dcTree = dcTreeUtils.getDocCommentTree(element);
            //System.out.println("dcTree: " + dcTree);
            
            if (element.getKind().isField() || element.getKind().equals(ElementKind.METHOD)) {
                System.out.println("element is field or method");
                
                try {
                    System.out.println(elementsUtils.getAllAnnotationMirrors(element));
                    
                } catch (Exception e) {}
                
            }
            
        }
    }
    
    private void emitClassDocs(DocletEnvironment env, PrintWriter pw, String packURL, Element cls) {
        pw.format("  <dt><a href=\"%s%s.html\">%s</a></dt>\n", packURL,
                qualifiedSimpleName(cls), qualifiedSimpleName(cls));

        // Print out all fields
        String fields = cls.getEnclosedElements()
                .stream()
                .filter(element -> element.getKind().isField())
                .filter(field -> field.getModifiers().contains(Modifier.PUBLIC))
                .map(field -> field.getSimpleName().toString())
                .collect(Collectors.joining(", "));

        if (!fields.isEmpty()) {
            pw.format("  <dd style='margin-bottom: 0.5em;'>%s</dd>\n", fields);
        }

        List<String> constructors = cls.getEnclosedElements()
                .stream()
                .filter(element -> ElementKind.CONSTRUCTOR == element.getKind())
                .filter(member -> member.getModifiers().contains(Modifier.PUBLIC))
                .map(member -> (ExecutableElement) member)
                .map(executableElement -> flatSignature(env, cls, executableElement))
                .collect(Collectors.toList());

        List<String> methods = cls.getEnclosedElements()
                .stream()
                .filter(element -> ElementKind.METHOD == element.getKind())
                .filter(member -> member.getModifiers().contains(Modifier.PUBLIC))
                .map(member -> (ExecutableElement) member)
                .map(executableElement -> flatSignature(env, cls, executableElement))
                .collect(Collectors.toList());

        List<String> members = new ArrayList<>(constructors);
        members.addAll(methods);

        // Print out all constructors and methods
        if (!members.isEmpty()) {
            pw.format("  <dd>%s</dd>\n", createMemberList(members));
        }

        Iterator<? extends Element> classesIterator = cls.getEnclosedElements()
                .stream()
                .filter(element -> element.getKind().isClass()
                        || element.getKind().isInterface()
                        || ElementKind.ENUM == element.getKind())
                .filter(element -> element.getModifiers().contains(Modifier.PUBLIC))
                .sorted(Comparator.comparing((Element o) -> o.getSimpleName().toString()))
                .iterator();
        if (classesIterator.hasNext()) {
            pw.print("\n");
        }
        while (classesIterator.hasNext()) {
            Element innerCls = classesIterator.next();
            // Each class links to Sun's main JavaDoc
            emitClassDocs(env, pw, packURL, innerCls);
            if (classesIterator.hasNext()) {
                pw.print("\n");
            }
        }
    }
    
    private String createMemberList(Collection<String> members) {
        StringBuilder sb = new StringBuilder();
        Iterator<String> iter = members.iterator();
        while (iter.hasNext()) {
            String member = iter.next();
            sb.append(member);
            if (iter.hasNext()) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    private String qualifiedSimpleName(Element element) {
        String elementName = element.getSimpleName().toString();
        if (ElementKind.PACKAGE != element.getEnclosingElement().getKind()) {
            return qualifiedSimpleName(element.getEnclosingElement()) + "." + elementName;
        }
        return elementName;
    }

    private String flatSignature(DocletEnvironment env, Element parent, ExecutableElement member) {
        return (ElementKind.CONSTRUCTOR == member.getKind()
                ? parent.getSimpleName().toString()
                : member.getSimpleName().toString()) +
                "(" + member.getParameters()
                .stream()
                .map(Element::asType)
                .map(t -> simpleParamName(env, t))
                .collect(Collectors.joining(", ")) + ")";
    }

    private String simpleParamName(DocletEnvironment env, TypeMirror type) {
        if (type.getKind().isPrimitive() || TypeKind.TYPEVAR == type.getKind()) {
            return String.valueOf(type);
        } else if (TypeKind.ARRAY == type.getKind()) {
            return simpleParamName(env, ((ArrayType) type).getComponentType()) + "[]";
        } else {
            return qualifiedSimpleName(env.getTypeUtils().asElement(type));
        }
    }
    
    @Override
    public Set<? extends Option> getSupportedOptions() {        
        return options;
    }

    private Stream<PackageElement> getSpecifiedPackages(DocletEnvironment root) {
        
        System.out.println("**1"+root.toString());
        
        return root.getSpecifiedElements()
                .stream()
                .filter(element -> {
                    System.out.println("element: " + element);
                    
                    //ElementKind.PACKAGE == element.getKind()
                    return true;})
                .map(element -> (PackageElement) element);
    }
    
    private final Set<Option> options = Set.of(
            // An option that takes no arguments.
            new Option("--alpha", false, "a flag", null) {
                @Override
                public boolean process(String option,
                                       List<String> arguments) {
                    alpha = true;
                    return true;
                }
            },
 
            // An option that takes a single string-valued argument.
            new Option("--output", true, "Output file path", "<string>") {
                @Override
                public boolean process(String option,
                                       List<String> arguments) {
                    outputFile = arguments.get(0);
                    
                    System.out.println("*************" + outputFile);
                    return true;
                }
            },
 
            // An option that takes a single integer-valued srgument.
            new Option("--gamma", true, "another option", "<int>") {
                @Override
                public boolean process(String option,
                                       List<String> arguments) {
                    String arg = arguments.get(0);
                    try {
                        gamma = Integer.parseInt(arg);
                        return true;
                    } catch (NumberFormatException e) {
                        // Note: it would be better to use
                        // {@link Reporter} to print an error message,
                        // so that the javadoc tool "knows" that an
                        // error was reported in conjunction\ with
                        // the "return false;" that follows.
                        System.err.println("not an int: " + arg);
                        return false;
                    }
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
