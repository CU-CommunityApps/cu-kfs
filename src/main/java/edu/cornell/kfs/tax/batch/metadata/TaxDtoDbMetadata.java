package edu.cornell.kfs.tax.batch.metadata;

import java.util.Map;

import org.kuali.kfs.krad.bo.BusinessObject;

public final class TaxDtoDbMetadata {

    private final Map<Class<? extends BusinessObject>, String> tableNames;
    private final Map<Class<? extends BusinessObject>, String> tableAliases;
    private final Map<Enum<?>, String> columnLabels;

    public TaxDtoDbMetadata(final Map<Class<? extends BusinessObject>, String> tableNames,
            final Map<Class<? extends BusinessObject>, String> tableAliases, final Map<Enum<?>, String> columnLabels) {
        this.tableNames = tableNames;
        this.tableAliases = tableAliases;
        this.columnLabels = columnLabels;
    }

    public String getQualifiedTableName(final Class<? extends BusinessObject> businessObjectClass) {
        return tableNames.get(businessObjectClass);
    }

    public String getTableAlias(final Class<? extends BusinessObject> businessObjectClass) {
        return tableAliases.get(businessObjectClass);
    }

    public String getFullColumnLabel(final Enum<?> fieldDefinition) {
        return columnLabels.get(fieldDefinition);
    }

}
