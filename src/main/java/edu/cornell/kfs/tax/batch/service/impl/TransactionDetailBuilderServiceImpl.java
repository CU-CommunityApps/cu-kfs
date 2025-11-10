package edu.cornell.kfs.tax.batch.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.function.FailableFunction;

import edu.cornell.kfs.tax.batch.TaxBatchConfig;
import edu.cornell.kfs.tax.batch.TaxStatistics;
import edu.cornell.kfs.tax.batch.TaxStatisticsHandler;
import edu.cornell.kfs.tax.batch.dataaccess.TaxDtoRowMapper;
import edu.cornell.kfs.tax.batch.dataaccess.TransactionDetailBuilderDao;
import edu.cornell.kfs.tax.batch.dto.DvSourceData;
import edu.cornell.kfs.tax.batch.service.TransactionDetailBuilderService;
import edu.cornell.kfs.tax.businessobject.TransactionDetail;
import edu.cornell.kfs.tax.dataaccess.impl.TaxStatType;
import edu.cornell.kfs.tax.service.TaxParameterService;

public class TransactionDetailBuilderServiceImpl implements TransactionDetailBuilderService {

    private static final int MAX_BATCH_INSERT_SIZE = 50;

    private TransactionDetailBuilderDao transactionDetailBuilderDao;
    private TaxParameterService taxParameterService;

    @Override
    public TaxStatistics generateTransactionDetails(final TaxBatchConfig config) {
        final TaxStatistics dvStatistics = transactionDetailBuilderDao.createDvTransactionDetails(
                config,
                (taxConfig, rowMapper) -> generateTransactionDetailsFromQueryResults(
                        taxConfig, rowMapper, this::generateTransactionDetailFromDvResult));
        return new TaxStatistics(dvStatistics);
    }

    private TransactionDetail generateTransactionDetailFromDvResult(final TaxDataHelper<DvSourceData> helper) {
        return null;
    }

    private <T> TaxStatistics generateTransactionDetailsFromQueryResults(final TaxBatchConfig config,
            final TaxDtoRowMapper<T> rowMapper,
            final FailableFunction<TaxDataHelper<T>, TransactionDetail, Exception> singleDetailGenerator)
                    throws Exception {
        final TaxDataHelper<T> helper = new TaxDataHelper<>(config, rowMapper);

        while (helper.rowMapper.moveToNextRow()) {
            final TransactionDetail transactionDetail = singleDetailGenerator.apply(helper);
            helper.pendingBatchInserts.add(transactionDetail);
        }

        return helper.statistics;
    }

    private static final class TaxDataHelper<T> implements TaxStatisticsHandler {

        private final TaxBatchConfig config;
        private final TaxDtoRowMapper<T> rowMapper;
        private final TaxStatistics statistics;
        private final List<TransactionDetail> pendingBatchInserts;

        private TaxDataHelper(final TaxBatchConfig config, final TaxDtoRowMapper<T> rowMapper) {
            this.config = config;
            this.rowMapper = rowMapper;
            this.statistics = new TaxStatistics();
            this.pendingBatchInserts = new ArrayList<>(MAX_BATCH_INSERT_SIZE);
        }

        @Override
        public void increment(TaxStatType entryType) {
            statistics.increment(entryType);
        }

        @Override
        public void increment(TaxStatType baseEntryType, String documentType) {
            statistics.increment(baseEntryType, documentType);
        }
        
    }

}
