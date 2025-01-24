package edu.cornell.kfs.tax.batch.util;

import java.sql.Types;
import java.util.Collection;
import java.util.function.Consumer;

public final class TaxQueryUtils {

    @FunctionalInterface
    public static interface Criteria extends Consumer<TaxQueryBuilder> {

        public static Criteria equal(final String propertyName, final String value) {
            return equal(propertyName, Types.VARCHAR, value);
        }

        public static Criteria equal(final String propertyName, final int sqlType, final Object value) {
            return builder -> builder.appendLeftHandSideOfEquals(propertyName)
                    .appendParameter(sqlType, value);
        }

        public static Criteria equal(final String propertyName, final TaxQueryBuilder subQuery) {
            return builder -> builder.appendLeftHandSideOfEquals(propertyName)
                    .appendSubQuery(subQuery);
        }

        public static Criteria equalToProperty(final String firstPropertyName, final String secondPropertyName) {
            return builder -> builder.appendLeftHandSideOfEquals(firstPropertyName)
                    .appendColumnForProperty(secondPropertyName);
        }

        public static Criteria notEqual(final String propertyName, final String value) {
            return notEqual(propertyName, Types.VARCHAR, value);
        }

        public static Criteria notEqual(final String propertyName, final int sqlType, final Object value) {
            return builder -> builder.appendLeftHandSideOfNotEqual(propertyName)
                    .appendParameter(sqlType, value);
        }

        public static Criteria notEqual(final String propertyName, final TaxQueryBuilder subQuery) {
            return builder -> builder.appendLeftHandSideOfNotEqual(propertyName)
                    .appendSubQuery(subQuery);
        }

        public static Criteria notEqualToProperty(final String firstPropertyName, final String secondPropertyName) {
            return builder -> builder.appendLeftHandSideOfNotEqual(firstPropertyName)
                    .appendColumnForProperty(secondPropertyName);
        }

        public static Criteria isNull(final String propertyName) {
            return builder -> builder.appendNullCheckCondition(propertyName, true);
        }

        public static Criteria isNotNull(final String propertyName) {
            return builder -> builder.appendNullCheckCondition(propertyName, false);
        }

        public static Criteria in(final String propertyName, final Collection<? extends String> values) {
            return in(propertyName, Types.VARCHAR, values);
        }

        public static Criteria in(final String propertyName, final int sqlType, final Collection<?> values) {
            return builder -> builder.appendInCondition(propertyName, sqlType, values);
        }

        public static Criteria notIn(final String propertyName, final Collection<? extends String> values) {
            return notIn(propertyName, Types.VARCHAR, values);
        }

        public static Criteria notIn(final String propertyName, final int sqlType, final Collection<?> values) {
            return builder -> builder.appendNotInCondition(propertyName, sqlType, values);
        }

        public static Criteria and(final Criteria... criteria) {
            return builder -> builder.appendAndCondition(true, criteria);
        }

        public static Criteria or(final Criteria... criteria) {
            return builder -> builder.appendOrCondition(true, criteria);
        }

    }

}
