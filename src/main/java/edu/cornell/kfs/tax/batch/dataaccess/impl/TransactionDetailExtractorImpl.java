package edu.cornell.kfs.tax.batch.dataaccess.impl;

import java.sql.ResultSet;

import edu.cornell.kfs.tax.batch.dataaccess.TransactionDetailExtractor;
import edu.cornell.kfs.tax.batch.metadata.TaxDtoMappingDefinition;
import edu.cornell.kfs.tax.businessobject.TransactionDetail;

public class TransactionDetailExtractorImpl<U> extends UpdatableTaxDataExtractorImpl<TransactionDetail, U>
        implements TransactionDetailExtractor<U> {

    public TransactionDetailExtractorImpl(final TaxDtoMappingDefinition<TransactionDetail> dtoDefinition,
            final TaxDtoMappingDefinition<U> dtoDefinitionForUpdates, final ResultSet resultSet) {
        super(dtoDefinition, dtoDefinitionForUpdates, resultSet);
    }

}
