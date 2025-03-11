package edu.cornell.kfs.tax.batch.service.impl;

import java.io.IOException;
import java.sql.SQLException;

import org.apache.commons.lang3.Validate;

import edu.cornell.kfs.tax.CUTaxConstants;
import edu.cornell.kfs.tax.batch.TaxBatchConfig;
import edu.cornell.kfs.tax.batch.TaxStatistics;
import edu.cornell.kfs.tax.batch.dataaccess.TaxDtoRowMapper;
import edu.cornell.kfs.tax.batch.dataaccess.TransactionDetailProcessorDao;
import edu.cornell.kfs.tax.batch.dto.SprintaxRowData;
import edu.cornell.kfs.tax.batch.service.TaxFileGenerationService;
import edu.cornell.kfs.tax.businessobject.TransactionDetail;
import edu.cornell.kfs.tax.businessobject.TransactionDetail.TransactionDetailField;

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

        return transactionDetailProcessorDao.processTransactionDetails(config, this::generateSprintaxFiles);
    }

    /*
     * This incomplete implementation is not actually writing the Sprintax files yet.
     * Future user stories will put such handling in place.
     */
    private TaxStatistics generateSprintaxFiles(final TaxBatchConfig config,
            final TaxDtoRowMapper<TransactionDetail> rowMapper) throws Exception {
        final SprintaxRowData sprintaxRow = new SprintaxRowData();
        sprintaxRow.setVendorName("Test Vendor");
        sprintaxRow.setForm1042SBox(CUTaxConstants.TAX_1042S_UNKNOWN_BOX_KEY);

        while (rowMapper.moveToNextRow()) {
            rowMapper.updateStringFieldsOnCurrentRow(sprintaxRow,
                    TransactionDetailField.vendorName, TransactionDetailField.form1042SBox);
        }

        return new TaxStatistics();
    }

    public void setTransactionDetailProcessorDao(final TransactionDetailProcessorDao transactionDetailProcessorDao) {
        this.transactionDetailProcessorDao = transactionDetailProcessorDao;
    }

}
