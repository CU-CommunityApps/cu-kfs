package edu.cornell.kfs.rass.batch;

import java.util.List;

public class RassSubObjectDefinition {

    private List<String> primaryKeyPropertyNames;
    private List<String> nonKeyPropertyNames;

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

}
