package edu.cornell.kfs.tax.batch.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.kuali.kfs.krad.bo.BusinessObject;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.util.CuSqlChunk;
import edu.cornell.kfs.sys.util.CuSqlQuery;
import edu.cornell.kfs.tax.batch.dataaccess.TaxDtoFieldEnum;
import edu.cornell.kfs.tax.batch.metadata.TaxDtoDbMetadata;
import edu.cornell.kfs.tax.batch.util.TaxQueryUtils.Criteria;
import edu.cornell.kfs.tax.batch.util.TaxQueryUtils.FieldUpdate;
import edu.cornell.kfs.tax.batch.util.TaxQueryUtils.QuerySort;
import edu.cornell.kfs.tax.batch.util.TaxQueryUtils.SqlFunction;

/**
 * Convenience class for building SELECT or UPDATE queries against tax-related data, using metadata helper objects
 * to handle converting BO classes and BO fields into table names and column names, respectively.
 * To ensure that the resulting SQL is valid, The calling code MUST build the clauses in the proper order
 * (SELECT, FROM, JOIN, WHERE, etc.).
 * 
 * When adding criteria to the query, the TaxQueryUtils.Criteria interface should be used to generate the appropriate
 * conditions. That interface has static factory methods to assist with building specific types of criteria (and,
 * or, equal, isNull, etc.).
 * 
 * If an ORDER BY clause is needed, create instances of TaxQueryUtils.QuerySort using one of the appropriate
 * static factory methods, then pass them into the builder's orderBy() method.
 * 
 * When building an UPDATE query, create instances of TaxQueryUtils.FieldUpdate using one of the appropriate
 * static factory methods, then pass them into the builder's set() method to construct the SET clause.
 * 
 * Note that many of the non-public methods in this class are declared as protected, so that they can be called
 * by the helper methods in TaxQueryUtils.
 */
public class TaxQueryBuilder {

    private final CuSqlChunk sqlChunk;
    private final TaxDtoDbMetadata mappingMetadata;

    public TaxQueryBuilder(final TaxDtoDbMetadata mappingMetadata) {
        Validate.notNull(mappingMetadata, "mappingMetadata cannot be null");
        this.sqlChunk = new CuSqlChunk();
        this.mappingMetadata = mappingMetadata;
    }



    public TaxQueryBuilder selectAllMappedFields() {
        final Class<? extends TaxDtoFieldEnum> enumClass = mappingMetadata.getFieldEnumClass();
        final Stream<TaxDtoFieldEnum> fields = Arrays.stream(enumClass.getEnumConstants());
        return select(fields);
    }

    public TaxQueryBuilder selectAllFieldsMappedTo(final Class<? extends BusinessObject> businessObjectClass) {
        final Class<? extends TaxDtoFieldEnum> enumClass = mappingMetadata.getFieldEnumClass();
        final Stream<TaxDtoFieldEnum> fields = Arrays.stream(enumClass.getEnumConstants());
        // Calling filter() on a separate line, because chaining it with the one above results in type mismatch errors.
        fields.filter(field -> businessObjectClass.equals(field.getMappedBusinessObjectClass()));
        return select(fields);
    }

    public TaxQueryBuilder select(final TaxDtoFieldEnum... fields) {
        return select(Stream.of(fields));
    }

    protected TaxQueryBuilder select(final Stream<TaxDtoFieldEnum> fields) {
        final String columnList = fields.map(this::getPotentiallyAliasedColumnLabelForSelectClause)
                .collect(Collectors.joining(CUKFSConstants.COMMA_AND_SPACE));
        sqlChunk.append("SELECT ", columnList);
        return this;
    }

    protected String getPotentiallyAliasedColumnLabelForSelectClause(final TaxDtoFieldEnum field) {
        if (field.needsExplicitAlias()) {
            final String fullLabel = getColumnLabel(field);
            final String alias = mappingMetadata.getColumnAlias(field);
            Validate.notBlank(alias, "Unexpected blank alias for field: " + field.name());
            return StringUtils.join(fullLabel, KFSConstants.BLANK_SPACE,
                    CUKFSConstants.DOUBLE_QUOTE, alias, CUKFSConstants.DOUBLE_QUOTE);
        } else {
            return getColumnLabel(field);
        }
    }

    public TaxQueryBuilder deleteFrom(final Class<? extends BusinessObject> baseBusinessObjectClass) {
        sqlChunk.append("DELETE");
        return from(baseBusinessObjectClass);
    }

    public TaxQueryBuilder from(final Class<? extends BusinessObject> baseBusinessObjectClass) {
        final String tableReference = getTableReferenceDeclaration(baseBusinessObjectClass);
        sqlChunk.append(" FROM ").append(tableReference);
        return this;
    }

    protected String getTableReferenceDeclaration(final Class<? extends BusinessObject> businessObjectClass) {
        final String qualifiedTableName = mappingMetadata.getQualifiedTableName(businessObjectClass);
        final String tableAlias = mappingMetadata.getTableAlias(businessObjectClass);
        Validate.notBlank(qualifiedTableName, "Table name not found for BO: " + businessObjectClass);
        Validate.notBlank(tableAlias, "Table alias not found for BO: " + businessObjectClass);
        return StringUtils.join(qualifiedTableName, KFSConstants.BLANK_SPACE, tableAlias);
    }

    public TaxQueryBuilder join(final Class<? extends BusinessObject> businessObjectClass,
            final Criteria... joinCriteria) {
        final String tableReference = getTableReferenceDeclaration(businessObjectClass);
        sqlChunk.append(" JOIN ").append(tableReference).append(" ON ");
        return appendAndCondition(false, joinCriteria);
    }

    public TaxQueryBuilder join(final TaxQueryBuilder subQuery,
            final Class<? extends BusinessObject> subQueryBusinessObjectClass,
            final Criteria... joinCriteria) {
        final String subQueryAlias = mappingMetadata.getTableAlias(subQueryBusinessObjectClass);
        sqlChunk.append(" JOIN ");
        appendSubQuery(subQuery);
        sqlChunk.append(KFSConstants.BLANK_SPACE).append(subQueryAlias).append(" ON ");
        return appendAndCondition(false, joinCriteria);
    }

    public TaxQueryBuilder leftJoin(final Class<? extends BusinessObject> businessObjectClass,
            final Criteria... joinCriteria) {
        final String tableReference = getTableReferenceDeclaration(businessObjectClass);
        sqlChunk.append(" LEFT JOIN ").append(tableReference).append(" ON ");
        return appendAndCondition(false, joinCriteria);
    }

    public TaxQueryBuilder where(final Criteria... criteria) {
        sqlChunk.append(" WHERE ");
        return appendAndCondition(false, criteria);
    }

    public TaxQueryBuilder unionAll(final TaxQueryBuilder siblingQuery) {
        sqlChunk.append(" UNION ALL ").append(siblingQuery.sqlChunk);
        return this;
    }

    public final TaxQueryBuilder orderBy(final QuerySort... fieldSorters) {
        Validate.notEmpty(fieldSorters, "At least one field sorter must be specified");
        sqlChunk.append(" ORDER BY ");
        int i = 0;
        for (final QuerySort fieldSorter : fieldSorters) {
            if (i > 0) {
                sqlChunk.append(CUKFSConstants.COMMA_AND_SPACE);
            }
            final String columnLabel = getColumnLabel(fieldSorter.getField());
            final String sortDirectionSql = fieldSorter.getSortDirectionSqlChunk();
            sqlChunk.append(columnLabel).append(KFSConstants.BLANK_SPACE).append(sortDirectionSql);
            i++;
        }
        return this;
    }

    public TaxQueryBuilder update(final Class<? extends BusinessObject> businessObjectClass) {
        final String tableReference = getTableReferenceDeclaration(businessObjectClass);
        sqlChunk.append("UPDATE ").append(tableReference);
        return this;
    }

    public TaxQueryBuilder set(final FieldUpdate... fieldUpdates) {
        sqlChunk.append(" SET ");
        int i = 0;
        for (final FieldUpdate fieldUpdate : fieldUpdates) {
            if (i > 0) {
                sqlChunk.append(CUKFSConstants.COMMA_AND_SPACE);
            }
            final String columnLabel = getColumnLabel(fieldUpdate.getField());
            sqlChunk.append(columnLabel).append(" = ");
            appendFieldUpdateValue(fieldUpdate);
            i++;
        }
        return this;
    }

    public TaxQueryBuilder insertValuesInto(final Class<? extends BusinessObject> businessObjectClass,
            final FieldUpdate... fieldUpdates) {
        final String tableReference = getTableReferenceDeclaration(businessObjectClass);
        sqlChunk.append("INSERT INTO ")
                .append(tableReference)
                .append(KFSConstants.BLANK_SPACE)
                .append(CUKFSConstants.LEFT_PARENTHESIS);

        int i = 0;
        for (final FieldUpdate fieldUpdate : fieldUpdates) {
            if (i > 0) {
                sqlChunk.append(CUKFSConstants.COMMA_AND_SPACE);
            }
            final String columnLabel = getColumnLabel(fieldUpdate.getField());
            sqlChunk.append(columnLabel);
            i++;
        }

        sqlChunk.append(CUKFSConstants.RIGHT_PARENTHESIS)
                .append(" VALUES ")
                .append(CUKFSConstants.LEFT_PARENTHESIS);

        i = 0;
        for (final FieldUpdate fieldUpdate : fieldUpdates) {
            if (i > 0) {
                sqlChunk.append(CUKFSConstants.COMMA_AND_SPACE);
            }
            appendFieldUpdateValue(fieldUpdate);
            i++;
        }

        sqlChunk.append(CUKFSConstants.RIGHT_PARENTHESIS);
        return this;
    }

    public CuSqlQuery build() {
        return sqlChunk.toQuery();
    }

    protected TaxQueryBuilder appendFieldUpdateValue(final FieldUpdate fieldUpdate) {
        if (fieldUpdate.getValue() instanceof CuSqlChunk) {
            sqlChunk.append((CuSqlChunk) fieldUpdate.getValue());
        } else {
            sqlChunk.appendAsParameter(fieldUpdate.getSqlType(), fieldUpdate.getValue());
        }
        return this;
    }

    protected TaxQueryBuilder appendNotAndCondition(final Criteria... criteria) {
        sqlChunk.append("NOT ");
        return appendAndCondition(true, criteria);
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
        for (final Criteria criterion : criteria) {
            if (i > 0) {
                sqlChunk.append(paddedOperand);
            }
            criterion.applyToQuery(this);
            i++;
        }

        if (addParentheses) {
            sqlChunk.append(CUKFSConstants.RIGHT_PARENTHESIS);
        }
        return this;
    }

    protected TaxQueryBuilder appendSqlFunction(final SqlFunction sqlFunction) {
        sqlFunction.applyToQuery(this);
        return this;
    }

    protected TaxQueryBuilder appendLeftHalfOfEqualityCondition(final TaxDtoFieldEnum field) {
        return appendLeftHandSideAndOperand(field, " = ");
    }

    protected TaxQueryBuilder appendLeftHalfOfInequalityCondition(final TaxDtoFieldEnum field) {
        return appendLeftHandSideAndOperand(field, " <> ");
    }

    protected TaxQueryBuilder appendLeftHalfOfBetweenCondition(final TaxDtoFieldEnum field) {
        return appendLeftHandSideAndOperand(field, " BETWEEN ");
    }

    protected TaxQueryBuilder appendLeftHalfOfLikeCondition(final TaxDtoFieldEnum field) {
        return appendLeftHandSideAndOperand(field, " LIKE ");
    }

    protected TaxQueryBuilder appendLeftHandSideAndOperand(final TaxDtoFieldEnum field, final String paddedOperand) {
        return appendColumnLabelForField(field)
                .appendSql(paddedOperand);
    }

    protected TaxQueryBuilder appendFunctionNameAndFirstOperand(final String functionName,
            final TaxDtoFieldEnum field) {
        return appendSql(functionName)
                .appendSql(CUKFSConstants.LEFT_PARENTHESIS)
                .appendColumnLabelForField(field);
    }

    protected TaxQueryBuilder appendFunctionNameAndFirstOperand(final String functionName,
            final SqlFunction nestedFunction) {
        return appendSql(functionName)
                .appendSql(CUKFSConstants.LEFT_PARENTHESIS)
                .appendSqlFunction(nestedFunction);
    }

    protected TaxQueryBuilder appendLastFunctionOperand(final int sqlType, final Object value) {
        return appendSql(CUKFSConstants.COMMA_AND_SPACE)
                .appendParameter(sqlType, value)
                .appendSql(CUKFSConstants.RIGHT_PARENTHESIS);
    }

    protected TaxQueryBuilder appendColumnLabelForField(final TaxDtoFieldEnum field) {
        final String columnLabel = getColumnLabel(field);
        sqlChunk.append(columnLabel);
        return this;
    }

    protected TaxQueryBuilder appendNullCheckCondition(final TaxDtoFieldEnum field, boolean positive) {
        return appendColumnLabelForField(field)
                .appendSql(positive ? " IS NULL" : " IS NOT NULL");
    }

    protected TaxQueryBuilder appendInCondition(final TaxDtoFieldEnum field, final int sqlType,
            final Collection<?> values) {
        final String columnLabel = getColumnLabel(field);
        sqlChunk.append(CuSqlChunk.asSqlInCondition(columnLabel, sqlType, values));
        return this;
    }

    protected TaxQueryBuilder appendInCondition(final TaxDtoFieldEnum field, final TaxQueryBuilder subQuery) {
        return appendLeftHandSideAndOperand(field, " IN ")
                .appendSubQuery(subQuery);
    }

    protected TaxQueryBuilder appendNotInCondition(final TaxDtoFieldEnum field, final int sqlType,
            final Collection<?> values) {
        final String columnLabel = getColumnLabel(field);
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

    protected String getColumnLabel(final TaxDtoFieldEnum field) {
        final String columnLabel = mappingMetadata.getFullColumnLabel(field);
        Validate.notBlank(columnLabel, "Unexpected field enum constant: " + field.name());
        return columnLabel;
    }

}
