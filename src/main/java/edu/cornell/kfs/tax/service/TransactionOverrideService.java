package edu.cornell.kfs.tax.service;

import java.util.List;

import edu.cornell.kfs.tax.businessobject.TransactionOverride;

public interface TransactionOverrideService {

    List<TransactionOverride> getTransactionOverrides(final String taxType, final java.sql.Date startDate,
            final java.sql.Date endDate);

}
