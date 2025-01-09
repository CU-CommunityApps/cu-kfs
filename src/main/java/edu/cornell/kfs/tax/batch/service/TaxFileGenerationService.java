package edu.cornell.kfs.tax.batch.service;

import java.io.IOException;
import java.sql.SQLException;

import edu.cornell.kfs.tax.batch.TaxBatchConfig;

public interface TaxFileGenerationService {

    Object generateFiles(final TaxBatchConfig config) throws IOException, SQLException;

}
