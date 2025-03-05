package edu.cornell.kfs.tax.batch.dataaccess;

import java.sql.SQLException;

/**
 * Helper interface for iterating over ResultSets and converting their rows into DTOs (or for updating
 * the rows using the DTO data).
 * 
 * The "T" type represents the DTO type that will be extracted from the ResultSet.
 * 
 * A DTO type other than the "T" type may be used when calling the updateStringFieldsOnCurrentRow() method,
 * as long as the DTO fields being accessed have the same names as those on the "T" DTO.
 */
public interface TaxDtoRowMapper<T> {

    boolean moveToNextRow() throws SQLException;

    T readCurrentRow() throws SQLException;

    void updateStringFieldsOnCurrentRow(final Object dtoContainingUpdates,
            final TaxDtoFieldEnum... fieldsToUpdate) throws SQLException;

}
