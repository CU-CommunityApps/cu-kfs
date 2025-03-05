package edu.cornell.kfs.tax.batch.dataaccess.impl;

import java.sql.SQLException;
import java.util.List;

import edu.cornell.kfs.tax.batch.dataaccess.TaxDtoFieldEnum;
import edu.cornell.kfs.tax.batch.dataaccess.TaxDtoRowMapper;

public class TaxDtoRowMapperTestImpl<T> implements TaxDtoRowMapper<T> {

    private final List<T> dtos;
    private int currentIndex;

    public TaxDtoRowMapperTestImpl(final List<T> dtos) {
        this.dtos = dtos;
        this.currentIndex = -1;
    }

    @Override
    public boolean moveToNextRow() throws SQLException {
        if (currentIndex == dtos.size()) {
            throw new IllegalStateException("Mapper already returned false on a previous call");
        }
        currentIndex++;
        return currentIndex < dtos.size();
    }

    @Override
    public T readCurrentRow() throws SQLException {
        return dtos.get(currentIndex);
    }

    @Override
    public void updateStringFieldsOnCurrentRow(final Object dtoContainingUpdates,
            final TaxDtoFieldEnum... fieldsToUpdate) throws SQLException {
        throw new UnsupportedOperationException("This implementation does not support updates");
    }

}
