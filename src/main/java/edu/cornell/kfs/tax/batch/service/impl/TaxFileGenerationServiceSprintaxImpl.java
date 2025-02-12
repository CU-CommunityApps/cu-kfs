package edu.cornell.kfs.tax.batch.service.impl;

import java.io.IOException;
import java.sql.SQLException;

import org.apache.commons.lang3.Validate;

import edu.cornell.kfs.tax.batch.TaxBatchConfig;
import edu.cornell.kfs.tax.batch.TaxStatistics;
import edu.cornell.kfs.tax.batch.dataaccess.TransactionDetailProcessorDao;
import edu.cornell.kfs.tax.batch.dataaccess.TransactionDetailRowMapper;
import edu.cornell.kfs.tax.batch.dataaccess.impl.TransactionDetailMapperForSprintax;
import edu.cornell.kfs.tax.batch.dto.SprintaxInfo1042S;
import edu.cornell.kfs.tax.batch.service.TaxFileGenerationService;

/*
 * This service will be fully implemented in a follow-up user story.
 */
public class TaxFileGenerationServiceSprintaxImpl implements TaxFileGenerationService {

    private TransactionDetailProcessorDao transactionDetailProcessorDao;

    @Override
    public Object generateFiles(final TaxBatchConfig config) throws IOException, SQLException {
        Validate.notNull(config, "config cannot be null");
        Validate.isTrue(config.getMode() == TaxBatchConfig.Mode.CREATE_TAX_FILES,
                "config should have specified CREATE_TAX_FILES mode");

        return transactionDetailProcessorDao.processTransactionDetails(
                config, TransactionDetailMapperForSprintax::new, this::generateSprintaxFiles);
    }

    /*
     * This incomplete implementation is not actually writing the Sprintax files yet.
     * Future user stories will put such handling in place.
     */
    private TaxStatistics generateSprintaxFiles(final TaxBatchConfig config,
            final TransactionDetailRowMapper<SprintaxInfo1042S> rowMapper) throws Exception {
        final SprintaxInfo1042S sprintaxInfo = new SprintaxInfo1042S();
        sprintaxInfo.setVendorNameForOutput("Test Vendor");

        while (rowMapper.moveToNextRow()) {
            rowMapper.updateCurrentRow(sprintaxInfo);
        }

        return new TaxStatistics();
    }

    public void setTransactionDetailProcessorDao(final TransactionDetailProcessorDao transactionDetailProcessorDao) {
        this.transactionDetailProcessorDao = transactionDetailProcessorDao;
    }

}
