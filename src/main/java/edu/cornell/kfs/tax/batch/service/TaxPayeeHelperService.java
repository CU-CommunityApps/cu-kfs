package edu.cornell.kfs.tax.batch.service;

import java.sql.SQLException;
import java.util.function.Supplier;

import edu.cornell.kfs.tax.batch.TaxStatisticsHandler;
import edu.cornell.kfs.tax.batch.dto.TaxPayeeBase;
import edu.cornell.kfs.tax.businessobject.TransactionDetail;

public interface TaxPayeeHelperService {

    <T extends TaxPayeeBase> T createTaxPayeeWithPopulatedVendorData(final Supplier<T> payeeConstructor,
            final TransactionDetail transactionDetail, final TaxStatisticsHandler statistics) throws SQLException;

}
