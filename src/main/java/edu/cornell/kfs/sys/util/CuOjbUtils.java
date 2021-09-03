package edu.cornell.kfs.sys.util;

import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class CuOjbUtils {

    public static Stream<Object[]> buildStreamForReportQueryResults(Iterator<?> reportQueryIterator) {
        return buildStreamForQueryResults(Object[].class, reportQueryIterator);
    }

    public static <T> Stream<T> buildStreamForQueryResults(Class<T> queryResultType, Iterator<?> queryIterator) {
        Objects.requireNonNull(queryResultType, "queryResultType cannot be null");
        Objects.requireNonNull(queryIterator, "queryIterator cannot be null");
        return StreamSupport.stream(() -> new QuerySpliterator<>(queryResultType, queryIterator), 0, false);
    }

    private static class QuerySpliterator<T> implements Spliterator<T> {
        private Class<T> queryResultType;
        private Iterator<?> queryIterator;

        private QuerySpliterator(Class<T> queryResultType, Iterator<?> queryIterator) {
            this.queryResultType = queryResultType;
            this.queryIterator = queryIterator;
        }

        @Override
        public boolean tryAdvance(Consumer<? super T> action) {
            if (queryIterator.hasNext()) {
                action.accept(queryResultType.cast(queryIterator.next()));
                return true;
            } else {
                return false;
            }
        }

        @Override
        public void forEachRemaining(Consumer<? super T> action) {
            while (queryIterator.hasNext()) {
                action.accept(queryResultType.cast(queryIterator.next()));
            }
        }

        @Override
        public Spliterator<T> trySplit() {
            return null;
        }

        @Override
        public long estimateSize() {
            return Long.MAX_VALUE;
        }

        @Override
        public int characteristics() {
            return 0;
        }
    }

}
