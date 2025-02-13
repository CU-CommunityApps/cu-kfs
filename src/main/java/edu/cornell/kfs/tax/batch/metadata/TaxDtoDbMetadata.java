package edu.cornell.kfs.tax.batch.metadata;

import java.util.Map;

import org.kuali.kfs.krad.bo.BusinessObject;

import edu.cornell.kfs.tax.batch.dataaccess.TaxDtoFieldEnum;

public final class TaxDtoDbMetadata {

    private final Map<Class<? extends BusinessObject>, String> tableNames;
    private final Map<Class<? extends BusinessObject>, String> tableAliases;
    private final Class<? extends TaxDtoFieldEnum> fieldEnumClass;
    private final Map<TaxDtoFieldEnum, String> columnLabels;

    public TaxDtoDbMetadata(final Map<Class<? extends BusinessObject>, String> tableNames,
            final Map<Class<? extends BusinessObject>, String> tableAliases,
            final Class<? extends TaxDtoFieldEnum> fieldEnumClass, final Map<TaxDtoFieldEnum, String> columnLabels) {
        this.tableNames = tableNames;
        this.tableAliases = tableAliases;
        this.fieldEnumClass = fieldEnumClass;
        this.columnLabels = columnLabels;
    }

    public Class<? extends TaxDtoFieldEnum> getFieldEnumClass() {
        return fieldEnumClass;
    }

    public String getQualifiedTableName(final Class<? extends BusinessObject> businessObjectClass) {
        return tableNames.get(businessObjectClass);
    }

    public String getTableAlias(final Class<? extends BusinessObject> businessObjectClass) {
        return tableAliases.get(businessObjectClass);
    }

    public String getFullColumnLabel(final TaxDtoFieldEnum fieldDefinition) {
        return columnLabels.get(fieldDefinition);
    }

    public int getMappedTableCount() {
        return tableNames.size();
    }

    public int getMappedColumnCount() {
        return columnLabels.size();
    }

}
