package edu.cornell.kfs.tax.batch.util;

import java.sql.Types;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.function.Consumer;

import edu.cornell.kfs.tax.batch.dataaccess.TaxDtoFieldEnum;

/**
 * Utility class containing two static nested classes and interfaces that work in conjunction with TaxQueryBuilder:
 * 
 * - QuerySort: Builds helper objects for controlling what fields to sort the query on and in which direction.
 * 
 * - Criteria: Builds helper objects for preparing the query criteria in the JOIN and WHERE clauses. (Note that
 *             the static factory methods create lambda expressions that perform the actual work; the lambdas
 *             will get invoked by the TaxQueryBuilder when it appends the JOIN and WHERE clauses to the main query.)
 * 
 * Both of these classes and interfaces are meant to be instantiated using one of the provided static factory methods.
 */
public final class TaxQueryUtils {

    public static final class QuerySort extends AbstractMap.SimpleImmutableEntry<TaxDtoFieldEnum, String> {

        private static final long serialVersionUID = 1L;

        private QuerySort(final TaxDtoFieldEnum field, final String sortDirectionSqlChunk) {
            super(field, sortDirectionSqlChunk);
        }

        public TaxDtoFieldEnum getField() {
            return getKey();
        }

        public String getSortDirectionSqlChunk() {
            return getValue();
        }

        public static QuerySort ascending(final TaxDtoFieldEnum field) {
            return new QuerySort(field, "ASC");
        }

        public static QuerySort ascendingNullsFirst(final TaxDtoFieldEnum field) {
            return new QuerySort(field, "ASC NULLS FIRST");
        }

        public static QuerySort descending(final TaxDtoFieldEnum field) {
            return new QuerySort(field, "DESC");
        }

    }

    @FunctionalInterface
    public static interface Criteria extends Consumer<TaxQueryBuilder> {

        public static Criteria equal(final TaxDtoFieldEnum field, final String value) {
            return equal(field, Types.VARCHAR, value);
        }

        public static Criteria equal(final TaxDtoFieldEnum field, final int sqlType, final Object value) {
            return builder -> builder.appendLeftHalfOfEqualityCondition(field)
                    .appendParameter(sqlType, value);
        }

        public static Criteria equal(final TaxDtoFieldEnum field, final TaxQueryBuilder subQuery) {
            return builder -> builder.appendLeftHalfOfEqualityCondition(field)
                    .appendSubQuery(subQuery);
        }

        public static Criteria equal(final TaxDtoFieldEnum firstField, final TaxDtoFieldEnum secondField) {
            return builder -> builder.appendLeftHalfOfEqualityCondition(firstField)
                    .appendColumnLabelForField(secondField);
        }

        public static Criteria notEqual(final TaxDtoFieldEnum field, final String value) {
            return notEqual(field, Types.VARCHAR, value);
        }

        public static Criteria notEqual(final TaxDtoFieldEnum field, final int sqlType, final Object value) {
            return builder -> builder.appendLeftHalfOfInequalityCondition(field)
                    .appendParameter(sqlType, value);
        }

        public static Criteria notEqual(final TaxDtoFieldEnum field, final TaxQueryBuilder subQuery) {
            return builder -> builder.appendLeftHalfOfInequalityCondition(field)
                    .appendSubQuery(subQuery);
        }

        public static Criteria notEqual(final TaxDtoFieldEnum firstField, final TaxDtoFieldEnum secondField) {
            return builder -> builder.appendLeftHalfOfInequalityCondition(firstField)
                    .appendColumnLabelForField(secondField);
        }

        public static Criteria isNull(final TaxDtoFieldEnum field) {
            return builder -> builder.appendNullCheckCondition(field, true);
        }

        public static Criteria isNotNull(final TaxDtoFieldEnum field) {
            return builder -> builder.appendNullCheckCondition(field, false);
        }

        public static Criteria in(final TaxDtoFieldEnum field, final Collection<? extends String> values) {
            return in(field, Types.VARCHAR, values);
        }

        public static Criteria in(final TaxDtoFieldEnum field, final int sqlType, final Collection<?> values) {
            return builder -> builder.appendInCondition(field, sqlType, values);
        }

        public static Criteria notIn(final TaxDtoFieldEnum field, final Collection<? extends String> values) {
            return notIn(field, Types.VARCHAR, values);
        }

        public static Criteria notIn(final TaxDtoFieldEnum field, final int sqlType, final Collection<?> values) {
            return builder -> builder.appendNotInCondition(field, sqlType, values);
        }

        public static Criteria and(final Criteria... criteria) {
            return builder -> builder.appendAndCondition(true, criteria);
        }

        public static Criteria or(final Criteria... criteria) {
            return builder -> builder.appendOrCondition(true, criteria);
        }

    }

}
