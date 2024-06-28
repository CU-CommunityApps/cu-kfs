package edu.cornell.kfs.sys.util;

import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.ojb.broker.accesslayer.OJBIterator;

public final class CuOjbUtils {

    public static <T> Stream<T> buildCloseableStreamForReportQueryResults(
            final Supplier<Iterator<?>> reportQueryOperation, Function<Object[], T> rowConverter) {
        Objects.requireNonNull(rowConverter, "rowConverter cannot be null");
        final Stream<Object[]> resultsStream = buildCloseableStreamForReportQueryResults(reportQueryOperation);
        return resultsStream.map(rowConverter);
    }

    public static Stream<Object[]> buildCloseableStreamForReportQueryResults(
            final Supplier<Iterator<?>> reportQueryOperation) {
        return buildCloseableStreamForQueryResults(Object[].class, reportQueryOperation);
    }

    @SuppressWarnings("unchecked")
    public static <T> Stream<T> buildCloseableStreamForQueryResults(final Class<T> queryResultType,
            final Supplier<Iterator<?>> queryOperation) {
        Objects.requireNonNull(queryResultType, "queryResultType cannot be null");
        Objects.requireNonNull(queryOperation, "queryOperation cannot be null");

        final Iterator<?> queryIterator = queryOperation.get();
        if (!(queryIterator instanceof OJBIterator)) {
            throw new IllegalStateException("Query result was either null or not an OJB iterator implementation");
        }
        final OJBIterator ojbIterator = (OJBIterator) queryIterator;
        boolean streamSetupSuccessful = false;

        try {
            final Spliterator<Object> querySpliterator = Spliterators.spliteratorUnknownSize(ojbIterator, 0);
            final Stream<T> resultsStream = StreamSupport.stream(() -> querySpliterator, 0, false)
                    .onClose(() -> closeOJBIteratorQuietly(ojbIterator))
                    .map(queryResultType::cast);
            streamSetupSuccessful = true;
            return resultsStream;
        } finally {
            if (!streamSetupSuccessful) {
                closeOJBIteratorQuietly(ojbIterator);
            }
        }
    }

    public static void closeOJBIteratorQuietly(final OJBIterator ojbIterator) {
        try {
            if (ojbIterator != null) {
                ojbIterator.releaseDbResources();
            }
        } catch (Exception e) {
            // Ignore
        }
    }

}
