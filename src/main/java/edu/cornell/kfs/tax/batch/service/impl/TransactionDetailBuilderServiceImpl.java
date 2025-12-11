package edu.cornell.kfs.tax.batch.service.impl;

import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.tax.batch.TaxBatchConfig;
import edu.cornell.kfs.tax.batch.TaxStatistics;
import edu.cornell.kfs.tax.batch.dataaccess.TaxDtoRowMapper;
import edu.cornell.kfs.tax.batch.dataaccess.TransactionDetailBuilderDao;
import edu.cornell.kfs.tax.batch.dto.DvSourceData;
import edu.cornell.kfs.tax.batch.service.TransactionDetailBuilderHelperService;
import edu.cornell.kfs.tax.batch.service.TransactionDetailBuilderService;
import edu.cornell.kfs.tax.service.TaxParameterService;

public class TransactionDetailBuilderServiceImpl implements TransactionDetailBuilderService {

    private TransactionDetailBuilderDao transactionDetailBuilderDao;
    private TaxParameterService taxParameterService;

    @Override
    public TaxStatistics generateTransactionDetails(final TaxBatchConfig config) {
        final TransactionDetailBuilderHelperService helperService =
                getNewTransactionDetailBuilderHelperServiceFromPrototype();
        final TaxStatistics dvStatistics = transactionDetailBuilderDao.createDvTransactionDetails(
                config, (taxConfig, rowMapper) -> generateDvTransactionDetails(taxConfig, rowMapper, helperService));
        return new TaxStatistics(dvStatistics);
    }

    private TaxStatistics generateDvTransactionDetails(
            final TaxBatchConfig config, final TaxDtoRowMapper<DvSourceData> rowMapper,
            final TransactionDetailBuilderHelperService helperService) throws Exception {
        final TransactionDetailGeneratorDV detailGenerator = new TransactionDetailGeneratorDV(
                config, rowMapper, helperService);
        return detailGenerator.generateAndInsertTransactionDetails();
    }

    private TransactionDetailBuilderHelperService getNewTransactionDetailBuilderHelperServiceFromPrototype() {
        return SpringContext.getBean(TransactionDetailBuilderHelperService.class);
    }

}
