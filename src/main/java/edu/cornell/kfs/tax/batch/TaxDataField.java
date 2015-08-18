package edu.cornell.kfs.tax.batch;

/**
 * Helper object that either defines a mapping from a DB-backed tax field to a String alias,
 * or defines the data type to use for a non-DB-backed tax field.
 * 
 * <p>For a DB-backed field, the following setup is allowed:</p>
 * 
 * <ul>
 *   <li>propertyName - The fully-qualified object class, followed by "::", followed by the object property's name.</li>
 *   <li>propertyAlias - The alias to use for the field; all aliases within a data row should be unique.</li>
 *   <li>skip - Indicates whether to explicitly exclude this field from querying or processing; default is false.</li>
 * </ul>
 * 
 * <p>For a non-DB-backed field, the following setup is allowed:</p>
 * 
 * <ul>
 *   <li>propertyName - The alias to use for the field; all aliases within a data row should be unique.</li>
 *   <li>propertyType - The data type to use for the field; should be the name of a java.sql.Types constant.</li>
 * </ul>
 * 
 * <p>NOTE: Do *NOT* set both propertyAlias and propertyType for a given field!
 * They are not designed to be used in conjunction with each other!
 * (DB-backed fields will have their data types derived at runtime.)</p>
 */
public final class TaxDataField {

    private String propertyName;
    private String propertyData;
    private boolean skip;

    public TaxDataField() {
        // Do nothing.
    }

    /**
     * Gets the object-and-propName combo for a DB-backed field, or the alias for a non-DB-backed field.
     */
    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    /**
     * Gets the property alias for a DB-backed field; should not be used by non-DB-backed fields.
     */
    public String getPropertyAlias() {
        return propertyData;
    }

    public void setPropertyAlias(String propertyAlias) {
        this.propertyData = propertyAlias;
    }

    /**
     * Gets the property data type (as a java.sql.Types constant name) for a non-DB-backed field;
     * should not be used by DB-backed fields.
     */
    public String getPropertyType() {
        return propertyData;
    }

    public void setPropertyType(String propertyType) {
        this.propertyData = propertyType;
    }

    /**
     * Indicates whether a DB-backed field should be excluded from querying or processing;
     * default is false, and should not be used by non-DB-backed fields.
     */
    public boolean isSkip() {
        return skip;
    }

    public void setSkip(boolean skip) {
        this.skip = skip;
    }

}
