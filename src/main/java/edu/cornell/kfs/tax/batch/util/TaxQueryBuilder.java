package edu.cornell.kfs.tax.batch.util;

import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.util.CuSqlChunk;
import edu.cornell.kfs.sys.util.CuSqlQuery;
import edu.cornell.kfs.tax.batch.metadata.TaxDtoFieldDefinition;
import edu.cornell.kfs.tax.batch.metadata.TaxDtoMappingDefinition;
import edu.cornell.kfs.tax.batch.metadata.TaxDtoTableDefinition;
import edu.cornell.kfs.tax.batch.util.TaxQueryConstants.SortOrder;
import edu.cornell.kfs.tax.batch.util.TaxQueryUtils.Criteria;

/**
 * Convenience class for building SELECT queries against tax-related data, using ORM-metadata-derived helper objects
 * to handle converting BO classes and property names into table names and column names, respectively.
 * 
 * When building a query that joins multiple business objects, all property names should be prefixed with the expected
 * table aliases, as defined by the TaxBusinessObjectMapping sub-annotations within the DTO's TaxDto annotation.
 * A dot (.) character should be used to separate the table alias from the local property name in such cases.
 * 
 * When adding criteria to the query, the TaxQueryUtils.Criteria interface should be used to generate the appropriate
 * conditions. That interface has static helper methods to assist with building specific types of criteria (and,
 * or, equal, isNull, etc.).
 */
public class TaxQueryBuilder {

    private final CuSqlChunk sqlChunk;
    private final TaxDtoMappingDefinition<?> dtoDefinition;
    private final Map<Class<?>, TaxDtoTableDefinition> tableMappings;
    private final Map<String, TaxDtoFieldDefinition<?, ?>> fieldMappings;

    public TaxQueryBuilder(final TaxDtoMappingDefinition<?> dtoDefinition) {
        Validate.notNull(dtoDefinition, "dtoDefinition cannot be null");
        this.sqlChunk = new CuSqlChunk();
        this.dtoDefinition = dtoDefinition;
        this.tableMappings = generateTableMappings(dtoDefinition);
        this.fieldMappings = generateFieldMappings(dtoDefinition);
    }

    private static Map<Class<?>, TaxDtoTableDefinition> generateTableMappings(
            final TaxDtoMappingDefinition<?> dtoDefinition) {
        return dtoDefinition.getBusinessObjectMappings().stream()
                .collect(Collectors.toUnmodifiableMap(
                        TaxDtoTableDefinition::getBusinessObjectClass, Function.identity()));
    }

    private static Map<String, TaxDtoFieldDefinition<?, ?>> generateFieldMappings(
            final TaxDtoMappingDefinition<?> dtoDefinition) {
        return dtoDefinition.getFieldMappings().stream()
                .collect(Collectors.toUnmodifiableMap(
                        TaxQueryBuilder::getPropertyKey, Function.identity()));
    }

    private static String getPropertyKey(final TaxDtoFieldDefinition<?, ?> dtoField) {
        final String columnLabel = dtoField.getColumnLabel();
        if (StringUtils.contains(columnLabel, KFSConstants.DELIMITER)) {
            return StringUtils.join(StringUtils.substringBefore(columnLabel, KFSConstants.DELIMITER),
                    KFSConstants.DELIMITER, dtoField.getPropertyName());
        } else {
            return dtoField.getPropertyName();
        }
    }



    public TaxQueryBuilder selectAllMappedFields() {
        final Stream<String> columnLabels = dtoDefinition.getFieldMappings().stream()
                .map(TaxDtoFieldDefinition::getColumnLabel);
        return select(columnLabels);
    }

    public TaxQueryBuilder select(final String... fieldNames) {
        final Stream<String> columnLabels = Stream.of(fieldNames)
                .map(this::getColumnLabel);
        return select(columnLabels);
    }

    protected TaxQueryBuilder select(final Stream<String> columnLabels) {
        final String columnList = columnLabels.collect(Collectors.joining(CUKFSConstants.COMMA_AND_SPACE));
        sqlChunk.append("SELECT ", columnList);
        return this;
    }

    public TaxQueryBuilder from(final Class<?> baseBusinessObjectClass) {
        final String tableReference = getTableReferenceDeclaration(baseBusinessObjectClass);
        sqlChunk.append(" FROM ").append(tableReference);
        return this;
    }

    protected String getTableReferenceDeclaration(final Class<?> businessObjectClass) {
        final TaxDtoTableDefinition dtoTable = getTableDefinition(businessObjectClass);
        if (dtoTable.hasTableAliasForQuery()) {
            return StringUtils.join(dtoTable.getTableName(), KFSConstants.BLANK_SPACE,
                    dtoTable.getTableAliasForQuery());
        } else {
            return dtoTable.getTableName();
        }
    }

    public TaxQueryBuilder join(final Class<?> businessObjectClass,
            final Criteria... joinCriteria) {
        final String tableReference = getTableReferenceDeclaration(businessObjectClass);
        sqlChunk.append(" JOIN ").append(tableReference).append(" ON ");
        return appendAndCondition(false, joinCriteria);
    }

    public TaxQueryBuilder where(final Criteria... criteria) {
        sqlChunk.append(" WHERE ");
        return appendAndCondition(false, criteria);
    }

    @SafeVarargs
    public final TaxQueryBuilder orderBy(final Pair<String, SortOrder>... fieldSortOrders) {
        Validate.notEmpty(fieldSortOrders, "At least one sort order must be specified");
        sqlChunk.append(" ORDER BY ");
        int i = 0;
        for (final Pair<String, SortOrder> fieldSortOrder : fieldSortOrders) {
            if (i > 0) {
                sqlChunk.append(CUKFSConstants.COMMA_AND_SPACE);
            }
            final String columnLabel = getColumnLabel(fieldSortOrder.getLeft());
            final SortOrder sortOrder = fieldSortOrder.getRight();
            sqlChunk.append(columnLabel).append(KFSConstants.BLANK_SPACE).append(sortOrder.toString());
            i++;
        }
        return this;
    }

    public CuSqlQuery build() {
        return sqlChunk.toQuery();
    }



    protected TaxQueryBuilder appendAndCondition(final boolean addParentheses, final Criteria... criteria) {
        return appendMultiPartCondition(" AND ", addParentheses, criteria);
    }

    protected TaxQueryBuilder appendOrCondition(final boolean addParentheses, final Criteria... criteria) {
        return appendMultiPartCondition(" OR ", addParentheses, criteria);
    }

    protected TaxQueryBuilder appendMultiPartCondition(final String paddedOperand, final boolean addParentheses,
            final Criteria... criteria) {
        Validate.isTrue(ArrayUtils.isNotEmpty(criteria), "At least one criterion must be specified");
        if (addParentheses) {
            sqlChunk.append(CUKFSConstants.LEFT_PARENTHESIS);
        }

        int i = 0;
        for (final Consumer<TaxQueryBuilder> criterion : criteria) {
            if (i > 0) {
                sqlChunk.append(paddedOperand);
            }
            criterion.accept(this);
            i++;
        }

        if (addParentheses) {
            sqlChunk.append(CUKFSConstants.RIGHT_PARENTHESIS);
        }
        return this;
    }

    protected TaxQueryBuilder appendLeftHandSideOfEquals(final String propertyName) {
        return appendLeftHandSideAndOperand(propertyName, " = ");
    }

    protected TaxQueryBuilder appendLeftHandSideOfNotEqual(final String propertyName) {
        return appendLeftHandSideAndOperand(propertyName, " <> ");
    }

    protected TaxQueryBuilder appendLeftHandSideAndOperand(final String propertyName, final String paddedOperand) {
        return appendColumnForProperty(propertyName)
                .appendSql(paddedOperand);
    }

    protected TaxQueryBuilder appendColumnForProperty(final String propertyName) {
        final String columnLabel = getColumnLabel(propertyName);
        sqlChunk.append(columnLabel);
        return this;
    }

    protected TaxQueryBuilder appendNullCheckCondition(final String propertyName, boolean checkForNull) {
        return appendColumnForProperty(propertyName)
                .appendSql(checkForNull ? " IS NULL" : " IS NOT NULL");
    }

    protected TaxQueryBuilder appendInCondition(final String propertyName, final int sqlType,
            final Collection<?> values) {
        final String columnLabel = getColumnLabel(propertyName);
        sqlChunk.append(CuSqlChunk.asSqlInCondition(columnLabel, sqlType, values));
        return this;
    }

    protected TaxQueryBuilder appendNotInCondition(final String propertyName, final int sqlType,
            final Collection<?> values) {
        final String columnLabel = getColumnLabel(propertyName);
        sqlChunk.append(CuSqlChunk.asSqlNotInCondition(columnLabel, sqlType, values));
        return this;
    }

    protected TaxQueryBuilder appendParameter(final int sqlType, final Object value) {
        sqlChunk.appendAsParameter(sqlType, value);
        return this;
    }

    protected TaxQueryBuilder appendSubQuery(final TaxQueryBuilder subQuery) {
        sqlChunk.append(CUKFSConstants.LEFT_PARENTHESIS)
                .append(subQuery.sqlChunk)
                .append(CUKFSConstants.RIGHT_PARENTHESIS);
        return this;
    }

    protected TaxQueryBuilder appendSql(final CharSequence sql) {
        sqlChunk.append(sql);
        return this;
    }

    protected String getColumnLabel(final String propertyName) {
        return getFieldDefinition(propertyName).getColumnLabel();
    }

    protected TaxDtoFieldDefinition<?, ?> getFieldDefinition(final String propertyName) {
        final TaxDtoFieldDefinition<?, ?> dtoField = fieldMappings.get(propertyName);
        Validate.isTrue(dtoField != null, "Unknown DTO property: %s", propertyName);
        return dtoField;
    }

    protected TaxDtoTableDefinition getTableDefinition(final Class<?> businessObjectClass) {
        final TaxDtoTableDefinition dtoTable = tableMappings.get(businessObjectClass);
        Validate.isTrue(dtoTable != null, "Unknown DTO business object mapping: %s", businessObjectClass);
        return dtoTable;
    }

}
