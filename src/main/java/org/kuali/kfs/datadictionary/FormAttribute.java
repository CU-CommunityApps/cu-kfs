/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2024 Kuali, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.kfs.datadictionary;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import edu.cornell.kfs.sys.businessobject.serialization.FormAttributeDefaultValueSerializer;

/**
 * ====
 * CU Customization: Added a custom JSON serializer on the "defaultValue" field to allow
 *                   our custom Create Done Batch File lookup to work properly.
 * ====
 * 
 * The data-source-agnostic representation of the form version of an attribute (adds information for how to display
 * this attribute in a form). Ignore all fields that we don't want to serialize in API payloads.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FormAttribute extends Attribute {

    private boolean canLookup;
    private Control control;
    // CU Customization: Add JsonSerialize annotation to this field.
    @JsonSerialize(using = FormAttributeDefaultValueSerializer.class)
    private String defaultValue;
    /**
     * This field is used to indicate a lookup should not be provided. Since we programmatically determine
     * when a lookup is available and display it by default, this allows for configuration that overrides that
     * calculation and prevents a lookup from being displayed. Note: as this is only part of the determination of
     * whether a lookup should be displayed or not, this is not currently provided in API responses explicitly but
     * rather it used as part the logic involved in setting {@link #canLookup}.
     */
    @JsonIgnore
    private boolean disableLookup;
    private String lookupClassName;
    private Map<String, String> lookupRelationshipMappings;
    private Validations validations;

    public boolean getCanLookup() {
        return canLookup;
    }

    public void setCanLookup(final boolean canLookup) {
        this.canLookup = canLookup;
    }

    public Control getControl() {
        return control;
    }

    public void setControl(final Control control) {
        this.control = control;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(final String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean getDisableLookup() {
        return disableLookup;
    }

    public void setDisableLookup(final boolean disableLookup) {
        this.disableLookup = disableLookup;
    }

    public String getLookupClassName() {
        return lookupClassName;
    }

    public void setLookupClassName(final String lookupClass) {
        lookupClassName = lookupClass;
    }

    public Map<String, String> getLookupRelationshipMappings() {
        return lookupRelationshipMappings;
    }

    public void setLookupRelationshipMappings(final Map<String, String> lookupRelationshipMappings) {
        this.lookupRelationshipMappings = lookupRelationshipMappings;
    }

    public Validations getValidations() {
        return validations;
    }

    public void setValidations(final Validations validations) {
        this.validations = validations;
    }

    // this part will probably need to continue to evolve but this gives us the divorce from validationPattern for now
    public static class Validations {

        private boolean disallowWildcards;
        private int scale;
        private boolean required;

        public int getScale() {
            return scale;
        }

        public void setScale(final int scale) {
            this.scale = scale;
        }

        public boolean isRequired() {
            return required;
        }

        public void setRequired(final boolean required) {
            this.required = required;
        }

        public boolean getDisallowWildcards() {
            return disallowWildcards;
        }

        public void setDisallowWildcards(final boolean disallowWildcards) {
            this.disallowWildcards = disallowWildcards;
        }
    }
}
