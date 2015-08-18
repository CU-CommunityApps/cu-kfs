package edu.cornell.kfs.tax.batch;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper object defining which objects (if any) to use for querying tax data
 * of a particular type, as well as defining how the object fields map to
 * various tax field aliases.
 */
public class TaxDataRow {

    private String rowId;
    private List<String> objectClasses;
    private List<TaxDataField> dataFields;
    private int numAutoAssignedFieldsForInsert;
    private boolean explicitFieldsOnly;

    public TaxDataRow() {
        this.objectClasses = new ArrayList<String>();
        this.dataFields = new ArrayList<TaxDataField>();
    }

    /**
     * Gets the unique ID that distinguishes this TaxDataRow from the others.
     */
    public String getRowId() {
        return rowId;
    }

    public void setRowId(String rowId) {
        this.rowId = rowId;
    }

    /**
     * Gets the list of object classes to use for querying the data from the database;
     * may be empty if the row and its fields are not directly mapped to the database.
     */
    public List<String> getObjectClasses() {
        return objectClasses;
    }

    public void setObjectClasses(List<String> objectClasses) {
        this.objectClasses = objectClasses;
    }

    public void addObjectClass(String objectClass) {
        objectClasses.add(objectClass);
    }

    /**
     * Returns the TaxDataField objects defining the queried or mapped fields.
     */
    public List<TaxDataField> getDataFields() {
        return dataFields;
    }

    public void setDataFields(List<TaxDataField> dataFields) {
        this.dataFields = dataFields;
    }

    public void addDataField(TaxDataField dataField) {
        dataFields.add(dataField);
    }

    /**
     * Returns the number of fields at the beginning of the INSERT query (if applicable) that will have
     * their values inserted in a manner other than parameter placeholders; default is zero.
     */
    public int getNumAutoAssignedFieldsForInsert() {
        return numAutoAssignedFieldsForInsert;
    }

    public void setNumAutoAssignedFieldsForInsert(int numAutoAssignedFieldsForInsert) {
        this.numAutoAssignedFieldsForInsert = numAutoAssignedFieldsForInsert;
    }

    /**
     * Returns whether the generated SELECT query should only retrieve the columns that have an explicit
     * TaxDataField mapping, as opposed to just retrieving all the object fields; default is false.
     */
    public boolean isExplicitFieldsOnly() {
        return explicitFieldsOnly;
    }

    public void setExplicitFieldsOnly(boolean explicitFieldsOnly) {
        this.explicitFieldsOnly = explicitFieldsOnly;
    }

}
