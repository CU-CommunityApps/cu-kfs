package edu.cornell.kfs.module.cam.batch.service.impl;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.SortedMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.module.cam.CamsConstants;
import org.kuali.kfs.module.cam.CamsKeyConstants;
import org.kuali.kfs.module.cam.CamsParameterConstants;
import org.kuali.kfs.module.cam.batch.AssetDepreciationStep;
import org.kuali.kfs.module.cam.batch.AssetPaymentInfo;
import org.kuali.kfs.module.cam.batch.service.impl.AssetDepreciationServiceImpl;
import org.kuali.kfs.module.cam.businessobject.AssetDepreciationTransaction;
import org.kuali.kfs.module.cam.businessobject.AssetObjectCode;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.businessobject.UniversityDate;

public class CuAssetDepreciationServiceImpl extends AssetDepreciationServiceImpl {
    private static final Logger LOG = LogManager.getLogger();
    
    @Override
    protected boolean runAssetDepreciation() throws ParseException {
        return true;
    }
    
    @Override
    public void runDepreciation() {
        LOG.debug("runDepreciation() started");

        Integer fiscalYear = -1;
        Integer fiscalMonth = -1;
        String errorMsg = "";
        List<String> documentNos = new ArrayList<>();
        List<String[]> reportLog = new ArrayList<>();
        Collection<AssetObjectCode> assetObjectCodes = new ArrayList<>();
        boolean hasErrors = false;
        LocalDate depreciationDate = dateTimeService.getLocalDateNow();
        String depreciationDateParameter = null;
        DateFormat dateFormat = new SimpleDateFormat(CamsConstants.DateFormats.YEAR_MONTH_DAY, Locale.US);
        boolean executeJob = false;
        String errorMessage = kualiConfigurationService.getPropertyValueAsString(CamsKeyConstants.Depreciation.DEPRECIATION_ALREADY_RAN_MSG);

        try {
            executeJob = runAssetDepreciation();
            if (executeJob) {
                LOG.info("*******{} HAS BEGUN *******", CamsConstants.Depreciation.DEPRECIATION_BATCH);
                if (parameterService.parameterExists(AssetDepreciationStep.class, CamsParameterConstants.DEPRECIATION_DATE_PARAMETER)) {
                    depreciationDateParameter = parameterService.getParameterValueAsString(AssetDepreciationStep.class,
                            CamsParameterConstants.DEPRECIATION_DATE_PARAMETER);
                }

                if (StringUtils.isBlank(depreciationDateParameter)) {
                    depreciationDateParameter = dateFormat.format(dateTimeService.getCurrentDate());
                }
                // This validates the system parameter depreciation_date has a valid format of YYYY-MM-DD.
                if (StringUtils.isNotBlank(depreciationDateParameter)) {
                    try {
                        depreciationDate =
                                dateTimeService.getLocalDate(dateFormat.parse(depreciationDateParameter.trim()));
                    } catch (final ParseException e) {
                        throw new IllegalArgumentException(kualiConfigurationService.getPropertyValueAsString(
                                CamsKeyConstants.Depreciation.INVALID_DEPRECIATION_DATE_FORMAT));
                    }
                }
                LOG.info(
                        "{}Depreciation run date: {}",
                        CamsConstants.Depreciation.DEPRECIATION_BATCH,
                        depreciationDateParameter
                );

                /**
                 * CU Customization to use java.sql.Date
                 */
                
                final UniversityDate universityDate = businessObjectService.findBySinglePrimaryKey(UniversityDate.class,
                        new java.sql.Date((dateTimeService.getUtilDate(depreciationDate)).getTime()));
                if (universityDate == null) {
                    throw new IllegalStateException(kualiConfigurationService.getPropertyValueAsString(
                            KFSKeyConstants.ERROR_UNIV_DATE_NOT_FOUND));
                }

                fiscalYear = universityDate.getUniversityFiscalYear();
                fiscalMonth = new Integer(universityDate.getUniversityFiscalAccountingPeriod());
                assetObjectCodes = getAssetObjectCodes(fiscalYear);
                // If the depreciation date is not = to the system date then, the depreciation process cannot run.
                LOG.info(
                        "{}Fiscal Year = {} & Fiscal Period={}",
                        CamsConstants.Depreciation.DEPRECIATION_BATCH,
                        fiscalYear,
                        fiscalMonth
                );

                int fiscalStartMonth = Integer.parseInt(optionsService.getCurrentYearOptions().getUniversityFiscalYearStartMo());
                reportLog.addAll(depreciableAssetsDao.generateStatistics(
                        true, 
                        null, 
                        fiscalYear, 
                        fiscalMonth, 
                        depreciationDate,
                        dateTimeService.toDateString(dateTimeService.getUtilDate(depreciationDate)), 
                        assetObjectCodes, 
                        fiscalStartMonth, 
                        errorMessage)
                );
                // update if fiscal period is 12
                // depreciationBatchDao.updateAssetsCreatedInLastFiscalPeriod(fiscalMonth, fiscalYear);
                updateAssetsDatesForLastFiscalPeriod(fiscalMonth, fiscalYear);
                // Retrieving eligible asset payment details
                LOG.info(
                        "{}Getting list of asset payments eligible for depreciation.",
                        CamsConstants.Depreciation.DEPRECIATION_BATCH
                );
                Collection<AssetPaymentInfo> depreciableAssetsCollection = depreciationBatchDao.getListOfDepreciableAssetPaymentInfo(fiscalYear, fiscalMonth,
                        depreciationDate);
                // if we have assets eligible for depreciation then, calculate depreciation and create glpe's
                // transactions
                if (depreciableAssetsCollection != null && !depreciableAssetsCollection.isEmpty()) {
                    SortedMap<String, AssetDepreciationTransaction> depreciationTransactions = this.calculateDepreciation(fiscalYear, fiscalMonth,
                            depreciableAssetsCollection, depreciationDate, assetObjectCodes);
                    processGeneralLedgerPendingEntry(fiscalYear, fiscalMonth, documentNos, depreciationTransactions);
                } else {
                    throw new IllegalStateException(
                            kualiConfigurationService.getPropertyValueAsString(CamsKeyConstants.Depreciation.NO_ELIGIBLE_FOR_DEPRECIATION_ASSETS_FOUND));
                }
            }
        } catch (Exception e) {
            LOG.error("Error occurred");
            LOG.error(
                    "{}**************************************************************************",
                    CamsConstants.Depreciation.DEPRECIATION_BATCH
            );
            LOG.error(
                    "{}AN ERROR HAS OCCURRED! - ERROR: {}",
                    () -> CamsConstants.Depreciation.DEPRECIATION_BATCH,
                    e::getMessage,
                    () -> e
            );
            LOG.error(
                    "{}**************************************************************************",
                    CamsConstants.Depreciation.DEPRECIATION_BATCH
            );
            hasErrors = true;
            errorMsg = "Depreciation process ran unsuccessfully.\nReason:" + e.getMessage();
        } finally {
            if (!hasErrors && executeJob) {
                int fiscalStartMonth = Integer.parseInt(optionsService.getCurrentYearOptions().getUniversityFiscalYearStartMo());
                reportLog.addAll(depreciableAssetsDao.generateStatistics(
                        false, 
                        documentNos, 
                        fiscalYear, 
                        fiscalMonth, 
                        depreciationDate,
                        dateTimeService.toDateString(dateTimeService.getUtilDate(depreciationDate)), 
                        assetObjectCodes, 
                        fiscalStartMonth, 
                        errorMessage)
                );
            }
            // the report will be generated only when there is an error or when the log has something.
            if (!reportLog.isEmpty() || !errorMsg.trim().equals("")) {
                reportService.generateDepreciationReport(reportLog, errorMsg, depreciationDateParameter);
            }

            LOG.debug("*******{} HAS ENDED *******", CamsConstants.Depreciation.DEPRECIATION_BATCH);
        }
    }
}
