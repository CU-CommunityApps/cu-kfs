package edu.cornell.kfs.tax.businessobject;

import java.util.HashMap;
import java.util.Map;

public class SecondPassAttributeUpdateValues {
    private Map<Integer, String> updateStringAttributeValues;
    private Map<Integer, java.sql.Date> updateDateAttributeValues;
    
    public SecondPassAttributeUpdateValues() {
        updateStringAttributeValues = new HashMap<Integer, String>();
        updateDateAttributeValues = new HashMap<Integer, java.sql.Date>();
    }

    public Map<Integer, String> getUpdateStringAttributeValues() {
        return updateStringAttributeValues;
    }

    public void setUpdateStringAttributeValues(Map<Integer, String> updateStringAttributeValues) {
        this.updateStringAttributeValues = updateStringAttributeValues;
    }

    public Map<Integer, java.sql.Date> getUpdateDateAttributeValues() {
        return updateDateAttributeValues;
    }

    public void setUpdateDateAttributeValues(Map<Integer, java.sql.Date> updateDateAttributeValues) {
        this.updateDateAttributeValues = updateDateAttributeValues;
    }
    
    public void putStringAttributeForUpdating(int columnIndex, String newValue) {
        updateStringAttributeValues.put(Integer.valueOf(columnIndex), newValue);
    }
    
    public void putSqlDateAttributeForUpdating(int columnIndex, java.sql.Date newValue) {
        updateDateAttributeValues.put(Integer.valueOf(columnIndex), newValue);
    }
}
