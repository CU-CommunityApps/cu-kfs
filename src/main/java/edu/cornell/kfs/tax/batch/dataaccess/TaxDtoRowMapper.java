package edu.cornell.kfs.tax.batch.dataaccess;

import java.sql.SQLException;

/**
 * Helper interface for iterating over ResultSets and converting their rows into DTOs.
 * 
 * The "T" type represents the DTO type that will be extracted from the ResultSet.
 */
public interface TaxDtoRowMapper<T> {

    boolean moveToNextRow() throws SQLException;

    T readCurrentRow() throws SQLException;

}
