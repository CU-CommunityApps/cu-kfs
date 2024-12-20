package edu.cornell.kfs.tax.dataaccess.impl;

/**
 * Helper class containing data about how DB-backed fields map to database columns,
 * or basic info concerning non-DB-backed fields. Is an immutable class.
 */
public final class TaxTableField {

    // The property name; either "FullyQualifiedObjectClass::propName" for DB-backed fields, or an alias for non-DB-backed fields.
    public final String propertyName;
    // The fully-qualified column name for DB-backed fields.
    public final String columnName;
    // The ResultSet column index for DB-backed fields.
    final int index;
    // The JDBC type of the field.
    final int jdbcType;

    TaxTableField(String propertyName, String columnName, int index, int jdbcType) {
        this.propertyName = propertyName;
        this.columnName = columnName;
        this.index = index;
        this.jdbcType = jdbcType;
    }

    /**
     * Returns just the relative portion of the column name.
     * 
     * @return The relative column name.
     */
    String getRelativeColumnName() {
        return (columnName != null) ? columnName.substring(columnName.lastIndexOf('.') + 1) : columnName;
    }

    /**
     * Only returns true when the other object is a non-null TaxTableField with an equal propertyName string value.
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof TaxTableField) {
            return (this.propertyName != null) ? this.propertyName.equals(((TaxTableField) o).propertyName) : ((TaxTableField) o).propertyName == null;
        }
        return false;
    }

    /**
     * Returns the hashCode of the propertyName string value, or zero if propertyName is null.
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return (this.propertyName != null) ? this.propertyName.hashCode() : 0;
    }

}
