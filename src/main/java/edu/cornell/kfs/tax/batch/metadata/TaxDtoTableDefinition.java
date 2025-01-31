package edu.cornell.kfs.tax.batch.metadata;

import java.util.Optional;

import org.apache.commons.lang3.Validate;
import org.kuali.kfs.krad.bo.BusinessObject;

public final class TaxDtoTableDefinition {

    private final Class<? extends BusinessObject> businessObjectClass;
    private final String tableName;
    private final Optional<String> tableAliasForQuery;

    public TaxDtoTableDefinition(final Class<? extends BusinessObject> businessObjectClass,
            final String tableName, final Optional<String> tableAliasForQuery) {
        Validate.notNull(businessObjectClass, "businessObjectClass cannot be null");
        Validate.notBlank(tableName, "tableName cannot be blank");
        Validate.notNull(tableAliasForQuery, "tableAliasForQuery wrapper cannot be null");

        this.businessObjectClass = businessObjectClass;
        this.tableName = tableName;
        this.tableAliasForQuery = tableAliasForQuery;
    }

    public Class<? extends BusinessObject> getBusinessObjectClass() {
        return businessObjectClass;
    }

    public String getTableName() {
        return tableName;
    }

    public String getTableAliasForQuery() {
        return tableAliasForQuery.get();
    }

    public boolean hasTableAliasForQuery() {
        return tableAliasForQuery.isPresent();
    }

}
