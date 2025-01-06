package edu.cornell.kfs.tax.batch.service;

import java.io.IOException;
import java.sql.SQLException;

import edu.cornell.kfs.tax.batch.TaxBatchConfig;
import edu.cornell.kfs.tax.batch.TaxStatistics;

public interface TaxFileGenerationService {

    TaxStatistics generateFiles(final TaxBatchConfig config) throws IOException, SQLException;

}
