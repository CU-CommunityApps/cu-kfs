package edu.cornell.kfs.tax.service.impl;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.api.datetime.DateTimeService;

import edu.cornell.kfs.tax.CUTaxConstants;
import edu.cornell.kfs.tax.CUTaxConstants.TaxCommonParameterNames;
import edu.cornell.kfs.tax.batch.TaxBatchConfig;
import edu.cornell.kfs.tax.batch.TaxStatistics;
import edu.cornell.kfs.tax.batch.service.TaxFileGenerationService;
import edu.cornell.kfs.tax.dataaccess.TaxProcessingDao;
import edu.cornell.kfs.tax.dataaccess.impl.TaxStatType;
import edu.cornell.kfs.tax.service.TaxParameterService;
import edu.cornell.kfs.tax.service.TaxProcessingV2Service;

public class TaxProcessingV2ServiceImpl implements TaxProcessingV2Service {

    private static final Logger LOG = LogManager.getLogger();

    private TaxProcessingDao legacyTaxProcessingDao;
    private TaxFileGenerationService taxFileGenerationServiceFor1042S;
    private TaxParameterService taxParameterService;
    private ConfigurationService configurationService;
    private DateTimeService dateTimeService;

    @Override
    public void performTaxProcessingFor1042S(final java.util.Date processingStartDate) {
        try {
            LOG.info("performTaxProcessingFor1042S, Starting 1042-S tax processing...");
            final TaxBatchConfig config = buildTaxBatchConfigFor1042S(processingStartDate);
            LOG.info("performTaxProcessingFor1042S, Finding transactions to process...");
            createTransactionDetailRowsFor1042SUsingLegacyProcess(config);
            LOG.info("performTaxProcessingFor1042S, Generating 1042-S files...");
            final TaxStatistics statistics = taxFileGenerationServiceFor1042S.generateFiles(config);
            LOG.info("performTaxProcessingFor1042S, Finished generating 1042-S files");
            printStatistics(statistics);
            LOG.info("performTaxProcessingFor1042S, Finished 1042-S tax processing");
        } catch (final Exception e) {
            LOG.error("performTaxProcessingFor1042S, Could not complete 1042-S processing", e);
            throw new RuntimeException(e);
        }
    }

    private void createTransactionDetailRowsFor1042SUsingLegacyProcess(final TaxBatchConfig config) {
        legacyTaxProcessingDao.doTaxProcessing(CUTaxConstants.TAX_TYPE_1042S_CREATE_TRANSACTION_ROWS_ONLY,
                config.getReportYear(), config.getStartDate(), config.getEndDate(), true,
                config.getProcessingStartDate());
    }

    private TaxBatchConfig buildTaxBatchConfigFor1042S(final java.util.Date processingStartDate) {
        return buildTaxBatchConfig(processingStartDate, CUTaxConstants.TAX_TYPE_1042S,
                CUTaxConstants.TAX_1042S_PARM_DETAIL);
    }

    private TaxBatchConfig buildTaxBatchConfig(final java.util.Date processingStartDate,
            final String taxType, final String taxParameterComponent) {
        final Collection<String> taxRangeSettings = taxParameterService.getParameterValuesAsString(
                taxParameterComponent, taxType + TaxCommonParameterNames.DATES_TO_PROCESS_PARAMETER_SUFFIX);
        final TaxBatchConfig taxConfig;
        if (taxRangeSettings.size() == 1) {
            final String taxRangeSetting = taxRangeSettings.iterator().next();
            taxConfig = buildTaxBatchConfigForSingleRangeSetting(processingStartDate, taxType, taxRangeSetting);
        } else if (taxRangeSettings.size() == 2) {
            taxConfig = buildTaxBatchConfigForExplicitStartAndEndDates(processingStartDate, taxType, taxRangeSettings);
        } else {
            throw new IllegalStateException("The dates-to-process parameter should only have either 1 value or 2 "
                    + "semicolon-delimited values");
        }

        LOG.info("buildTaxBatchConfig, Will perform {} processing for transactions in year {} between {} and {}",
                taxConfig.getTaxType(), taxConfig.getReportYear(), taxConfig.getStartDate(), taxConfig.getEndDate());

        return taxConfig;
    }

    private TaxBatchConfig buildTaxBatchConfigForSingleRangeSetting(final java.util.Date processingStartDate,
            final String taxType, final String taxRangeSetting) {
        final int reportYear = getReportYear(taxRangeSetting);
        final LocalDate startDate = LocalDate.of(reportYear, 1, 1);
        final LocalDate endDate = LocalDate.of(reportYear, 12, 31);
        return new TaxBatchConfig(taxType, reportYear, processingStartDate,
                java.sql.Date.valueOf(startDate), java.sql.Date.valueOf(endDate));
    }

    private int getReportYear(final String taxRangeSetting) {
        if (StringUtils.equals(taxRangeSetting, CUTaxConstants.YEAR_TO_DATE)) {
            return dateTimeService.getLocalDateNow().getYear();
        } else if (StringUtils.equals(taxRangeSetting, CUTaxConstants.PREVIOUS_YEAR_TO_DATE)) {
            return dateTimeService.getLocalDateNow().getYear() - 1;
        } else {
            try {
                return Integer.parseInt(taxRangeSetting);
            } catch (final NumberFormatException e) {
                throw new IllegalStateException("The dates-to-process parameter contained a malformed tax year");
            }
        }
    }

    private TaxBatchConfig buildTaxBatchConfigForExplicitStartAndEndDates(final java.util.Date processingStartDate,
            final String taxType, final Collection<String> taxRangeSettings) {
        try {
            final Iterator<String> literalDateValuesIterator = taxRangeSettings.iterator();
            final java.sql.Date startDate = dateTimeService.convertToSqlDate(literalDateValuesIterator.next());
            final java.sql.Date endDate = dateTimeService.convertToSqlDate(literalDateValuesIterator.next());
            final LocalDate localStartDate = startDate.toLocalDate();
            final LocalDate localEndDate = endDate.toLocalDate();
            final int reportYear = localStartDate.getYear();
            Validate.validState(localStartDate.getYear() == localEndDate.getYear(),
                    "Tax processing range cannot span more than one calendar year");
            Validate.validState(localStartDate.compareTo(localEndDate) <= 0,
                    "Tax processing range cannot have a start date that is after the end date");
            return new TaxBatchConfig(taxType, reportYear, processingStartDate, startDate, endDate);
        } catch (final ParseException e) {
            throw new IllegalStateException("Failed to parse start/end date for tax processing range", e);
        }
    }

    private void printStatistics(final TaxStatistics statistics) {
        LOG.info("printStatistics, ======================================");
        LOG.info("printStatistics, ============  STATISTICS  ============");
        LOG.info("printStatistics, ======================================");
        for (final Map.Entry<TaxStatType, Integer> statistic : statistics.getOrderedResults().entrySet()) {
            final String messageLabelKey = statistic.getKey().getPropKey();
            final String messageLabel = configurationService.getPropertyValueAsString(messageLabelKey);
            LOG.info("printStatistics, {}: {}", messageLabel, statistic.getValue());
        }
        LOG.info("printStatistics, ======================================");
        LOG.info("printStatistics, ==========  END STATISTICS  ==========");
        LOG.info("printStatistics, ======================================");
    }



    public void setLegacyTaxProcessingDao(final TaxProcessingDao legacyTaxProcessingDao) {
        this.legacyTaxProcessingDao = legacyTaxProcessingDao;
    }

    public void setTaxFileGenerationServiceFor1042S(final TaxFileGenerationService taxFileGenerationServiceFor1042S) {
        this.taxFileGenerationServiceFor1042S = taxFileGenerationServiceFor1042S;
    }

    public void setTaxParameterService(final TaxParameterService taxParameterService) {
        this.taxParameterService = taxParameterService;
    }

    public void setConfigurationService(final ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public void setDateTimeService(final DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

}
