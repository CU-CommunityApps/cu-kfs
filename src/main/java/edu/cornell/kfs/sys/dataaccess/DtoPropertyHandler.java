package edu.cornell.kfs.sys.dataaccess;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.BiConsumer;

import org.apache.commons.lang3.function.FailableBiFunction;

public final class DtoPropertyHandler<T, V> {

    private final String columnLabel;
    private final FailableBiFunction<ResultSet, String, V, SQLException> columnValueGetter;
    private final BiConsumer<T, V> dtoSetter;

    public DtoPropertyHandler(final String columnLabel,
            final FailableBiFunction<ResultSet, String, V, SQLException> columnValueGetter,
            final BiConsumer<T, V> dtoSetter) {
        this.columnLabel = columnLabel;
        this.columnValueGetter = columnValueGetter;
        this.dtoSetter = dtoSetter;
    }

    public void setProperty(final T dto, final ResultSet resultSet) throws SQLException {
        final V value = columnValueGetter.apply(resultSet, columnLabel);
        dtoSetter.accept(dto, value);
    }

    public String getColumnLabel() {
        return columnLabel;
    }

    public static <R> DtoPropertyHandler<R, String> forString(final String columnLabel,
            final BiConsumer<R, String> dtoSetter) {
        return new DtoPropertyHandler<>(columnLabel, ResultSet::getString, dtoSetter);
    }

    public static <R> DtoPropertyHandler<R, Integer> forInteger(final String columnLabel,
            final BiConsumer<R, Integer> dtoSetter) {
        return new DtoPropertyHandler<>(columnLabel, ResultSet::getInt, dtoSetter);
    }

    public static <R> DtoPropertyHandler<R, Long> forLong(final String columnLabel,
            final BiConsumer<R, Long> dtoSetter) {
        return new DtoPropertyHandler<>(columnLabel, ResultSet::getLong, dtoSetter);
    }

}
