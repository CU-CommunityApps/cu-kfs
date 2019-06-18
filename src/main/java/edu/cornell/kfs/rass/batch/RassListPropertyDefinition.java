package edu.cornell.kfs.rass.batch;

import java.util.Map;

public class RassListPropertyDefinition extends RassPropertyDefinition {

    private RassSubObjectDefinition subObjectDefinition;
    private Map<String, String> foreignKeyMappings;

    public RassSubObjectDefinition getSubObjectDefinition() {
        return subObjectDefinition;
    }

    public void setSubObjectDefinition(RassSubObjectDefinition subObjectDefinition) {
        this.subObjectDefinition = subObjectDefinition;
    }

    public Map<String, String> getForeignKeyMappings() {
        return foreignKeyMappings;
    }

    public void setForeignKeyMappings(Map<String, String> foreignKeyMappings) {
        this.foreignKeyMappings = foreignKeyMappings;
    }

}
