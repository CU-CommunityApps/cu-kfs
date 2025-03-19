package edu.cornell.kfs.tax.batch.service.impl;

import java.io.IOException;
import java.sql.SQLException;

import edu.cornell.kfs.tax.batch.TaxBatchConfig;
import edu.cornell.kfs.tax.batch.TaxStatistics;
import edu.cornell.kfs.tax.batch.service.TaxFileGenerationService;

public class TaxFileGenerationServiceNoOpImpl implements TaxFileGenerationService {

    @Override
    public TaxStatistics generateFiles(final TaxBatchConfig config) throws IOException, SQLException {
        return null;
    }

}
