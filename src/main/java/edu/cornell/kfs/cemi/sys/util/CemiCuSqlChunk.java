package edu.cornell.kfs.cemi.sys.util;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Validate;

import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.util.CuSqlChunk;

public final class CemiCuSqlChunk {
    
    public static CuSqlChunk asSqlBetweenCondition(final String columnName, final int sqlType,
            final Object rangeStartValue, final Object rangeEndValue) {
        return new CuSqlChunk()
                .append(columnName)
                .append(" BETWEEN ").appendAsParameter(sqlType, rangeStartValue)
                .append(" AND ").appendAsParameter(sqlType, rangeEndValue);
    }

    public static CuSqlChunk asListingOfColumnNames(final List<String> columnNames) {
        Validate.isTrue(CollectionUtils.isNotEmpty(columnNames), "columnNames cannot be null or empty");
        final CuSqlChunk sqlChunk = new CuSqlChunk();
        final Iterator<String> columnNamesIterator = columnNames.iterator();
        sqlChunk.append(columnNamesIterator.next());
        while (columnNamesIterator.hasNext()) {
            sqlChunk.append(CUKFSConstants.COMMA_AND_SPACE)
                    .append(columnNamesIterator.next());
        }
        return sqlChunk;
    }

    public static CuSqlChunk asListingOfStringArrayValuesForBatchUpdate(final int arrayLength) {
        Validate.isTrue(arrayLength > 0, "arrayLength must be a positive integer");
        final List<Function<? super String[], ?>> arrayValueGetters = new ArrayList<>(arrayLength);
        for (int i = 0; i < arrayLength; i++) {
            final int currentIndex = i;
            arrayValueGetters.add(stringArray -> stringArray[currentIndex]);
        }
        return asListingOfDataObjectPropertiesForBatchUpdate(Types.VARCHAR, arrayValueGetters);
    }

    public static <T> CuSqlChunk asListingOfDataObjectPropertiesForBatchUpdate(
            final int sqlType, final List<Function<? super T, ?>> propertyGetters) {
        Validate.isTrue(CollectionUtils.isNotEmpty(propertyGetters), "propertyGetters cannot be null or empty");
        final CuSqlChunk sqlChunk = new CuSqlChunk();
        final Iterator<Function<? super T, ?>> propertyGettersIterator = propertyGetters.iterator();
        sqlChunk.appendAsParameter(sqlType, propertyGettersIterator.next());
        while (propertyGettersIterator.hasNext()) {
            sqlChunk.append(CUKFSConstants.COMMA_AND_SPACE)
                    .appendAsParameter(sqlType, propertyGettersIterator.next());
        }
        return sqlChunk;
    }

}
