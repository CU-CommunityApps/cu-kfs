package edu.cornell.kfs.sys.util;

import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class CuOjbUtils {

    public static Stream<Object[]> buildStreamForReportQueryResults(Iterator<?> reportQueryIterator) {
        return buildStreamForQueryResults(Object[].class, reportQueryIterator);
    }

    @SuppressWarnings("unchecked")
    public static <T> Stream<T> buildStreamForQueryResults(Class<T> queryResultType, Iterator<?> queryIterator) {
        Objects.requireNonNull(queryResultType, "queryResultType cannot be null");
        Objects.requireNonNull(queryIterator, "queryIterator cannot be null");
        Spliterator<Object> querySpliterator = Spliterators.spliteratorUnknownSize(
                (Iterator<Object>) queryIterator, 0);
        return StreamSupport.stream(() -> querySpliterator, 0, false)
                .map(queryResultType::cast);
    }

}
