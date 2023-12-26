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
    
    private static final List<String> GRADLE_ANNOTATIONS = new ArrayList<>() {{
        add("Input");
        add("InputFile");
        add("Optional");
        add("OutputFile");
        add("OutputDirectory");
    }};
    
    private static final String TASK_ACTION_ANNOTATION = "TaskAction";

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
                    
                    
                    //DocTrees docTrees = env.getDocTrees();
                    //System.out.println("classes:" + ElementFilter.typesIn(env.getIncludedElements()));

                    // Beschreibungen (gemäss Spez) sämtlicher Element.
                    dcTreeUtils = env.getDocTrees();
                    
                    // Annotationen (und andere Infos) sämtlicher Elemente.
                    elementsUtils = env.getElementUtils();

                    while (classesIterator.hasNext()) {
                        Element cls = classesIterator.next();                                                
                        //TypeElement classElement = (TypeElement) cls;
                        System.out.println("className: " + cls.getSimpleName());
                        System.out.println("className: " + cls);

                        // Nur Klassen mit einer TaskAction-annotierten Methoden werden behandelt.
                        if (!findTaskAction(cls)) 
                            continue;

                        
                        TypeElement classElement = (TypeElement) cls;
                        
                        List<String> fields = new ArrayList<>();
                        getFieldsDummy(classElement, fields); 
                        
                        System.out.println("fields: " + fields);
                        
//                        System.out.println(classElement.getModifiers().size());
//                        classElement.getModifiers().stream().filter(m -> {
//                            System.out.println(m.name());
//                            System.out.println("*****");
//                            return true;
//                        });
                        
                        // Each class links to Oracle's main JavaDoc
//                        emitClassDocs(env, pw, packURL, cls);
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
    
    private void getFieldsDummy(Element cls, List<String> fields) {
        System.out.println("***"+cls.getSimpleName()+"***");
        for (Element element : cls.getEnclosedElements()) {
            if (element.getKind().isField()) {
                
                //System.out.println(elementsUtils.getAllAnnotationMirrors(element));
                boolean isGradleProperty = elementsUtils.getAllAnnotationMirrors(element).stream().map(annot -> {
                    String annotSimpleName = annot.toString().substring(annot.toString().lastIndexOf(".")+1);
                    return annotSimpleName;
                })
                .anyMatch(GRADLE_ANNOTATIONS::contains);
                
                if (!isGradleProperty) 
                    continue;

                System.out.println("---------------------");

                String name = element.getSimpleName().toString();
                System.out.println("name: " + name);
                if (dcTreeUtils.getDocCommentTree(element) != null) {
                    String description = dcTreeUtils.getDocCommentTree(element).toString();                    
                }
                String qualifiedFieldType = element.asType().toString();
                
                String unqualifiedFieldType = getUnqualifiedFieldType(qualifiedFieldType);
                System.out.println("unqualifiedFieldType: " + unqualifiedFieldType);
                
//                if (element.asType().getKind().equals(TypeKind.DECLARED)) {
//                    
//                    DeclaredType dType = (DeclaredType) element.asType();
//                    System.out.println(dType);
//                    System.out.println(dType.asElement());
//                }
                
                fields.add(element.getSimpleName() + " ---- " + element.asType());
                
            }
        }
        
        TypeElement classElement = (TypeElement) cls;        
        if (classElement.getSuperclass() != null) {
            Element scls = elementsUtils.getTypeElement(classElement.getSuperclass().toString());
            if (scls.getSimpleName().toString().equalsIgnoreCase("Object")) {
                return;
            }
            getFieldsDummy(scls, fields);
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
        // Print the result
        System.out.println("Split parts:");
        for (int i=0; i<partsBracket.length; i++) {
            System.out.println("part:" + partsBracket[i]);
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
            
//            unqualifiedFieldType.append(getUnqualifiedFieldType(partsBracket[i]));
//            if (i>0) {
//                unqualifiedFieldType.append(">");            
//            }
        }

        
        
//        if (!fieldType.contains(".")) {
//            return fieldType;
//        }
//        
//        if (!fieldType.contains("<")) {
//            return fieldType.substring(fieldType.lastIndexOf(".")+1);
//        }
//        
//        if (!fieldType.startsWith("<") && fieldType.contains("<")) {
//            unqualifiedFieldType.append(getUnqualifiedFieldType(fieldType.substring(0, fieldType.indexOf("<"))));
//        }
//      
//        System.out.println("unqualifiedFieldType (inside 1): " + unqualifiedFieldType);
        
        
        
        
//        Stack<Integer> stack = new Stack<>();
//        Stack<Integer> nested = new Stack<>();
//        boolean openingBracketFound = false;
//        for (int i = 0; i < fieldType.length(); i++) {
//            char currentChar = fieldType.charAt(i);
//            if (currentChar == '<' && !openingBracketFound) {
//                stack.push(i);
//                openingBracketFound = true;
//            } else if (currentChar == '>' && !stack.isEmpty()) {
//                int startIndex = stack.pop();
//                int endIndex = i;
//                String textBetweenBrackets = fieldType.substring(startIndex + 1, endIndex);
//                
//                unqualifiedFieldType.append("<");
//
//                String[] splittedFieldTypes = textBetweenBrackets.split(",");
//                for (String ft : splittedFieldTypes) {
//                    unqualifiedFieldType.append(getUnqualifiedFieldType(ft));                    
//                }
//                
//                unqualifiedFieldType.append(">");
//                
//                if (openingBracketFound) {
//                    unqualifiedFieldType.append(">");                    
//                }
//
//                //unqualifiedFieldType.append("<").append(getUnqualifiedFieldType(textBetweenBrackets)).append(">");
//                
//                System.out.println("1111Text between angle brackets: " + textBetweenBrackets);
//            } else if (currentChar == '<' && openingBracketFound) {
//                System.out.println("nested");
//                nested.push(i);
//                int startIndex = stack.pop();
//                int endIndex = i;
//                String textBetweenBrackets = fieldType.substring(startIndex + 1, endIndex);
//                System.out.println("textBetweenBrackets: " + textBetweenBrackets);
//                
//                unqualifiedFieldType.append("<").append(getUnqualifiedFieldType(textBetweenBrackets));
//                //System.out.println("2222Text between angle brackets: " + textBetweenBrackets);                
//                stack.push(i);
//            }
//        }

//        boolean insideBrackets = false;
//        StringBuilder contentInsideBrackets = new StringBuilder();
//
//        for (char c : fieldType.toCharArray()) {
//            if (c == '<' && !insideBrackets) {
//                insideBrackets = true;
//                System.out.println("erstes");
//            } else if (c == '<' && insideBrackets) {
//                System.out.println("nested");
//                System.out.println("Content inside angle brackets: " + contentInsideBrackets.toString());
//                contentInsideBrackets.setLength(0); // Clear the StringBuilder for the next match
//            } else if (c == '>' && insideBrackets) {
//                //insideBrackets = false;
//                System.out.println("Content inside angle brackets: " + contentInsideBrackets.toString());
//                contentInsideBrackets.setLength(0); // Clear the StringBuilder for the next match
//            } else if (insideBrackets) {
//                contentInsideBrackets.append(c);
//            }
//        }
//
//        /* 
//         * Wenn erstes "<", dann generic found. Zeichen sammeln, bis ">" oder nächstes "<". Zeichen ausgeben. 
//         */
//        
        
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
    
    private void getFields(TypeElement classElement, List<String> fields) {
        for (Element element : classElement.getEnclosedElements()) {
            System.out.println("element: " + element);
            System.out.println("kind: " + element.getKind());
            
            DocCommentTree dcTree = dcTreeUtils.getDocCommentTree(element);
            //System.out.println("dcTree: " + dcTree);
            
            if (element.getKind().isField() || element.getKind().equals(ElementKind.METHOD)) {
                System.out.println("element is field or method");
                
                System.out.println(element.asType());
                System.out.println(element.asType());
                System.out.println(element.asType().getKind());
                
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
        return root.getSpecifiedElements()
                .stream()
                .filter(element -> {                    
                    if (ElementKind.PACKAGE == element.getKind()) {
                        return true;
                    }
                    return false;
                    })
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
