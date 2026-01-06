package edu.cornell.kfs.tax.batch.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.tax.batch.TaxBatchConfig;
import edu.cornell.kfs.tax.batch.TaxStatistics;
import edu.cornell.kfs.tax.batch.dataaccess.TaxDtoRowMapper;
import edu.cornell.kfs.tax.batch.dataaccess.TransactionDetailBuilderDao;
import edu.cornell.kfs.tax.batch.dto.DvSourceData;
import edu.cornell.kfs.tax.batch.dto.PdpSourceData;
import edu.cornell.kfs.tax.batch.dto.PrncSourceData;
import edu.cornell.kfs.tax.batch.service.TransactionDetailBuilderHelperService;
import edu.cornell.kfs.tax.batch.service.TransactionDetailBuilderService;

public class TransactionDetailBuilderServiceImpl implements TransactionDetailBuilderService {

    private static final Logger LOG = LogManager.getLogger();

    private TransactionDetailBuilderDao transactionDetailBuilderDao;

    public void deleteTransactionDetailsForConfiguredTaxTypeAndYear(final TaxBatchConfig config) {
        LOG.info("deleteTransactionDetailsForConfiguredTaxTypeAndYear, Deleting {} transaction details for {} tax year",
                config.getTaxType(), config.getReportYear());
        transactionDetailBuilderDao.deleteTransactionDetailsForConfiguredTaxTypeAndYear(config);
    }

    @Override
    public TaxStatistics generateTransactionDetails(final TaxBatchConfig config) {
        LOG.info("generateTransactionDetails, Starting transaction detail generation");
        final TransactionDetailBuilderHelperService helperService =
                getNewInstanceOfTransactionDetailBuilderHelperService();
        final TaxStatistics dvStatistics = transactionDetailBuilderDao.createDvTransactionDetails(
                config, (taxConfig, rowMapper) -> generateDvTransactionDetails(taxConfig, rowMapper, helperService));
        final TaxStatistics pdpStatistics = transactionDetailBuilderDao.createPdpTransactionDetails(
                config, (taxConfig, rowMapper) -> generatePdpTransactionDetails(taxConfig, rowMapper, helperService));
        final TaxStatistics prncStatistics = transactionDetailBuilderDao.createPrncTransactionDetails(
                config, (taxConfig, rowMapper) -> generatePrncTransactionDetails(taxConfig, rowMapper, helperService));
        LOG.info("generateTransactionDetails, Finished transaction detail generation");
        return new TaxStatistics(dvStatistics, pdpStatistics, prncStatistics);
    }

    private TaxStatistics generateDvTransactionDetails(
            final TaxBatchConfig config, final TaxDtoRowMapper<DvSourceData> rowMapper,
            final TransactionDetailBuilderHelperService helperService) throws Exception {
        final TransactionDetailGeneratorDV detailGenerator = new TransactionDetailGeneratorDV(
                config, rowMapper, helperService);
        return detailGenerator.generateAndInsertTransactionDetails();
    }

    private TaxStatistics generatePdpTransactionDetails(
            final TaxBatchConfig config, final TaxDtoRowMapper<PdpSourceData> rowMapper,
            final TransactionDetailBuilderHelperService helperService) throws Exception {
        final TransactionDetailGeneratorPDP detailGenerator = new TransactionDetailGeneratorPDP(
                config, rowMapper, helperService);
        return detailGenerator.generateAndInsertTransactionDetails();
    }

    private TaxStatistics generatePrncTransactionDetails(
            final TaxBatchConfig config, final TaxDtoRowMapper<PrncSourceData> rowMapper,
            final TransactionDetailBuilderHelperService helperService) throws Exception {
        final TransactionDetailGeneratorPRNC detailGenerator = new TransactionDetailGeneratorPRNC(
                config, rowMapper, helperService);
        return detailGenerator.generateAndInsertTransactionDetails();
    }

    private TransactionDetailBuilderHelperService getNewInstanceOfTransactionDetailBuilderHelperService() {
        return SpringContext.getBean(TransactionDetailBuilderHelperService.class);
    }

    public void setTransactionDetailBuilderDao(final TransactionDetailBuilderDao transactionDetailBuilderDao) {
        this.transactionDetailBuilderDao = transactionDetailBuilderDao;
    }

}
