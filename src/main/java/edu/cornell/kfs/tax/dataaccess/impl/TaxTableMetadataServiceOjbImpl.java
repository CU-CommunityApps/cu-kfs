package edu.cornell.kfs.tax.dataaccess.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.ojb.broker.metadata.ClassDescriptor;
import org.apache.ojb.broker.metadata.FieldDescriptor;
import org.kuali.rice.krad.service.impl.PersistenceServiceStructureImplBase;

import edu.cornell.kfs.tax.batch.CUTaxBatchConstants;
import edu.cornell.kfs.tax.batch.TaxDataField;
import edu.cornell.kfs.tax.batch.TaxDataRow;

/**
 * Default TaxTableMetadataService implementation that converts OJB metadata
 * into tax-related metadata.
 */
public class TaxTableMetadataServiceOjbImpl extends PersistenceServiceStructureImplBase implements TaxTableMetadataService {

    @Override
    public <E extends TaxTableRow> E getRowFromData(TaxDataRow dataRow, Class<E> rowClazz) {
        if (dataRow == null) {
            throw new IllegalArgumentException("dataRow cannot be null");
        } else if (rowClazz == null) {
            throw new IllegalArgumentException("rowClazz cannot be null");
        }
        
        Map<String,TaxDataField> dataFields = new HashMap<String,TaxDataField>();
        Map<String,TaxTableField> fields = new LinkedHashMap<String,TaxTableField>();
        Map<String,TaxTableField> aliasedFields = new LinkedHashMap<String,TaxTableField>();
        List<String> tableNames = new ArrayList<String>();
        int columnIndex = 1;
        int tableIndex = 0;
        boolean explicitFieldsOnly = dataRow.isExplicitFieldsOnly();
        
        // Populate the data fields map.
        for (TaxDataField dataField : dataRow.getDataFields()) {
            dataFields.put(dataField.getPropertyName(), dataField);
        }
        
        // Obtain the relevant metadata from each referenced object class, and prepare to use it in building the TaxTableRow.
        for (String className : dataRow.getObjectClasses()) {
            Class<?> clazz;
            ClassDescriptor classDescriptor;
            // Get the OJB class descriptor.
            try {
                clazz = Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Could not find BO class: " + className);
            }
            classDescriptor = getClassDescriptor(clazz);
            
            // Add the descriptor's table name.
            tableNames.add(classDescriptor.getFullTableName());
            
            // Build a TaxTableField instance for each applicable OJB field descriptor on that class.
            for (FieldDescriptor fieldDescriptor : classDescriptor.getFieldDescriptions()) {
                boolean skipField = explicitFieldsOnly;
                String propertyName = className + CUTaxBatchConstants.CLASS_AND_PROPERTY_SEPARATOR + fieldDescriptor.getAttributeName();
                // Get the matching data field from the tax data definition file, if applicable.
                TaxDataField dataField = dataFields.get(propertyName);
                
                if (dataField != null) {
                    skipField = dataField.isSkip();
                }
                
                // Create a TaxTableField instance if implicit fields are allowed or if the data field object is not explicitly being skipped.
                if (!skipField) {
                    // Create a TaxTableField with a fully-alias-qualified column name.
                    TaxTableField tableField = new TaxTableField(propertyName,
                            new StringBuilder().append('A').append(tableIndex).append('.').append(fieldDescriptor.getColumnName()).toString(),
                            columnIndex, fieldDescriptor.getJdbcType().getType());
                    fields.put(propertyName, tableField);
                    if (dataField != null && StringUtils.isNotBlank(dataField.getPropertyAlias())) {
                        aliasedFields.put(dataField.getPropertyAlias(), tableField);
                    }
                    columnIndex++;
                }
            }
            
            tableIndex++;
        }
        
        // Build and return the TaxTableRow. See the base TaxTableRow class for details on the expected constructor arguments.
        try {
            return rowClazz.getDeclaredConstructor(String.class, Map.class, List.class, Map.class, Integer.class).newInstance(
                    dataRow.getRowId(), fields, tableNames, aliasedFields, Integer.valueOf(dataRow.getNumAutoAssignedFieldsForInsert()));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }



    @Override
    public <E extends TaxTableRow> E getTransientRowFromData(TaxDataRow dataRow, Class<E> rowClazz) {
        if (dataRow == null) {
            throw new IllegalArgumentException("dataRow arg cannot be null");
        } else if (rowClazz == null) {
            throw new IllegalArgumentException("rowClazz arg cannot be null");
        }
        
        Map<String,TaxTableField> fields = new LinkedHashMap<String,TaxTableField>();
        Map<String,TaxTableField> aliasedFields = new LinkedHashMap<String,TaxTableField>();
        int i = 0;
        
        // For each explicit data field from the tax data definition file, create a TaxTableField instance for it.
        for (TaxDataField dataField : dataRow.getDataFields()) {
            int fieldType;
            try {
                // Get the name of the constant containing the field's data type, then get the constant's value.
                fieldType = java.sql.Types.class.getField(dataField.getPropertyType()).getInt(null);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException("Could not find Java SQL type with name: " + dataField.getPropertyType());
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Invalid or inaccessible Java SQL type field: " + dataField.getPropertyType());
            }
            
            // Build the field.
            TaxTableField tableField = new TaxTableField(dataField.getPropertyName(), dataField.getPropertyName(), i, fieldType);
            fields.put(tableField.propertyName, tableField);
            aliasedFields.put(tableField.propertyName, tableField);
            i++;
        }
        
        // Build and return the TaxTableRow. See the base TaxTableRow class for details on the expected constructor arguments.
        try {
            return rowClazz.getDeclaredConstructor(String.class, Map.class, List.class, Map.class, Integer.class).newInstance(
                    dataRow.getRowId(), fields, Collections.emptyList(), aliasedFields, Integer.valueOf(0));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

}
