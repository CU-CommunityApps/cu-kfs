package edu.cornell.kfs.rass.batch;

import java.util.List;

import org.kuali.kfs.krad.bo.PersistableBusinessObject;

public class RassSubObjectDefinition {

    private Class<? extends PersistableBusinessObject> subObjectClass;
    private List<String> primaryKeyPropertyNames;
    private List<String> nonKeyPropertyNames;
    private String primaryIndicatorPropertyName;

    public Class<? extends PersistableBusinessObject> getSubObjectClass() {
        return subObjectClass;
    }

    public void setSubObjectClass(Class<? extends PersistableBusinessObject> subObjectClass) {
        this.subObjectClass = subObjectClass;
    }

    public List<String> getPrimaryKeyPropertyNames() {
        return primaryKeyPropertyNames;
    }

    public void setPrimaryKeyPropertyNames(List<String> primaryKeyPropertyNames) {
        this.primaryKeyPropertyNames = primaryKeyPropertyNames;
    }

    public List<String> getNonKeyPropertyNames() {
        return nonKeyPropertyNames;
    }

    public void setNonKeyPropertyNames(List<String> nonKeyPropertyNames) {
        this.nonKeyPropertyNames = nonKeyPropertyNames;
    }

    public String getPrimaryIndicatorPropertyName() {
        return primaryIndicatorPropertyName;
    }

    public void setPrimaryIndicatorPropertyName(String primaryIndicatorPropertyName) {
        this.primaryIndicatorPropertyName = primaryIndicatorPropertyName;
    }

}
