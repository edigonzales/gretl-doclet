package ch.so.agi.gretl.doclet;

import java.net.URI;

public class Property {
    public String name;
    public String description;
    public String type;
    public String qualifiedType;
    public URI javadocUrl;
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getQualifiedType() {
        return qualifiedType;
    }
    public void setQualifiedType(String qualifiedType) {
        this.qualifiedType = qualifiedType;
    }
    public URI getJavadocUrl() {
        return javadocUrl;
    }
    public void setJavadocUrl(URI javadocUrl) {
        this.javadocUrl = javadocUrl;
    }
}
