package edu.cornell.kfs.tax.service;

import java.time.LocalDateTime;

public interface TaxProcessingV2Service {

    void performTaxProcessingFor1042S(final LocalDateTime processingStartDate);

}
