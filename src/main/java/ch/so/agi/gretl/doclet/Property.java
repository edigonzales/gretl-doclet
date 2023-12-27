package ch.so.agi.gretl.doclet;

public class Property {
    public String name;
    public String description;
    public String type;
    public String qualifiedType;
    public boolean mandatory;
    
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
    public boolean isMandatory() {
        return mandatory;
    }
    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }
}
