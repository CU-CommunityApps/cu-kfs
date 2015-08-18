package edu.cornell.kfs.tax.dataaccess.impl;

import edu.cornell.kfs.tax.batch.TaxDataRow;

/**
 * Helper interface for taking ORM metadata and converting it into tax-related
 * metadata for use with JDBC-based tax data interaction.
 */
public interface TaxTableMetadataService {

    /**
     * Constructs a DB-mapped TaxTableRow instance using the given
     * TaxDataRow object and table row implementation class.
     * 
     * <p>NOTE: The tax table row class *MUST* have a constructor with
     * the same arguments as the abstract TaxTableRow superclass!</p>
     * 
     * @param dataRow The TaxDataRow object containing data on how a particular section of the tax data maps to the database.
     * @param rowClazz The TaxTableRow implementation class to instantiate.
     * @return A given TaxTableRow implementation object configured with the provided metadata.
     */
    <E extends TaxTableRow> E getRowFromData(TaxDataRow dataRow, Class<E> rowClazz);

    /**
     * Constructs a non-DB-mapped TaxTableRow instance using the given
     * TaxDataRow object and table row implementation class.
     * 
     * <p>NOTE: The tax table row class *MUST* have a constructor with
     * the same arguments as the abstract TaxTableRow superclass!</p>
     * 
     * @param dataRow The TaxDataRow object containing data on the non-DB-backed section of the tax data. 
     * @param rowClazz The TaxTableRow implementation class to instantiate.
     * @return A given TaxTableRow implementation object configured with the provided metadata.
     */
    <E extends TaxTableRow> E getTransientRowFromData(TaxDataRow dataRow, Class<E> rowClazz);
}
