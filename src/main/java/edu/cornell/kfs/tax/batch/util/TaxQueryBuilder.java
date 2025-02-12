package edu.cornell.kfs.tax.batch.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Consumer;
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
import edu.cornell.kfs.tax.batch.util.TaxQueryUtils.QuerySort;

/**
 * Convenience class for building SELECT queries against tax-related data, using metadata helper objects
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
        final Stream<String> columnLabels = Arrays.stream(enumClass.getEnumConstants())
                .map(this::getColumnLabel);
        return select(columnLabels);
    }

    public TaxQueryBuilder select(final TaxDtoFieldEnum... fields) {
        final Stream<String> columnLabels = Stream.of(fields)
                .map(this::getColumnLabel);
        return select(columnLabels);
    }

    protected TaxQueryBuilder select(final Stream<String> columnLabels) {
        final String columnList = columnLabels.collect(Collectors.joining(CUKFSConstants.COMMA_AND_SPACE));
        sqlChunk.append("SELECT ", columnList);
        return this;
    }

    public TaxQueryBuilder from(final Class<? extends BusinessObject> baseBusinessObjectClass) {
        final String tableReference = getTableReferenceDeclaration(baseBusinessObjectClass);
        sqlChunk.append(" FROM ").append(tableReference);
        return this;
    }

    protected String getTableReferenceDeclaration(final Class<? extends BusinessObject> businessObjectClass) {
        final String qualifiedTableName = mappingMetadata.getQualifiedTableName(businessObjectClass);
        final String tableAlias = mappingMetadata.getTableAlias(businessObjectClass);
        return StringUtils.join(qualifiedTableName, KFSConstants.BLANK_SPACE, tableAlias);
    }

    public TaxQueryBuilder join(final Class<? extends BusinessObject> businessObjectClass,
            final Criteria... joinCriteria) {
        final String tableReference = getTableReferenceDeclaration(businessObjectClass);
        sqlChunk.append(" JOIN ").append(tableReference).append(" ON ");
        return appendAndCondition(false, joinCriteria);
    }

    public TaxQueryBuilder where(final Criteria... criteria) {
        sqlChunk.append(" WHERE ");
        return appendAndCondition(false, criteria);
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

    protected TaxQueryBuilder appendLeftHalfOfEqualityCondition(final TaxDtoFieldEnum field) {
        return appendLeftHandSideAndOperand(field, " = ");
    }

    protected TaxQueryBuilder appendLeftHalfOfInequalityCondition(final TaxDtoFieldEnum field) {
        return appendLeftHandSideAndOperand(field, " <> ");
    }

    protected TaxQueryBuilder appendLeftHandSideAndOperand(final TaxDtoFieldEnum field, final String paddedOperand) {
        return appendColumnLabelForField(field)
                .appendSql(paddedOperand);
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
        final String columnLabel = mappingMetadata.getFullColumnLabel((Enum<?>) field);
        Validate.notBlank(columnLabel, "Unexpected field enum constant: " + field.name());
        return columnLabel;
    }

}
