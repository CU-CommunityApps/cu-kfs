package edu.cornell.kfs.tax.batch.dataaccess;

import java.sql.SQLException;

/**
 * Helper interface for iterating over ResultSets and converting their rows into DTOs (or for updating
 * the rows using the DTO data).
 * 
 * The "T" type represents the DTO type that will be extracted from the ResultSet.
 * 
 * The "U" type represents the DTO type that will be used for updating the ResultSet (if supported);
 * it can be the same as the "T" type if desired.
 */
public interface TaxDtoRowMapper<T, U> {

    boolean moveToNextRow() throws SQLException;

    T readCurrentRow() throws SQLException;

    void updateCurrentRow(final U dtoContainingUpdates) throws SQLException;

}
