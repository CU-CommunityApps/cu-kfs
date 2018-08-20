package edu.cornell.kfs.sys.batch.dataaccess.impl;

import java.sql.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.batch.FiscalYearMakerStep;
import org.kuali.kfs.sys.batch.dataaccess.impl.UniversityDateFiscalYearMakerImpl;
import org.kuali.kfs.sys.businessobject.UniversityDate;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.krad.util.ObjectUtils;

public class CuUniversityDateFiscalYearMakerImpl extends UniversityDateFiscalYearMakerImpl {

    private static final Logger LOG = LogManager.getLogger(CuUniversityDateFiscalYearMakerImpl.class);
    protected ParameterService parameterService;

    /**
     * @see org.kuali.kfs.coa.batch.dataaccess.impl.FiscalYearMakerHelperImpl#performCustomProcessing(java.lang.Integer)
     */
    @Override
    public void performCustomProcessing(Integer baseFiscalYear, boolean firstCopyYear) {
        int fiscalYearStartMonth = getFiscalYearStartMonth(baseFiscalYear);
        boolean replaceMode = parameterService.getParameterValueAsBoolean(FiscalYearMakerStep.class, KFSConstants.ChartApcParms.FISCAL_YEAR_MAKER_REPLACE_MODE);

        // determine start date year, if start month is not January the year will be one behind the fiscal year
        int startDateYear = baseFiscalYear;
        if (Calendar.JANUARY == fiscalYearStartMonth) {
            startDateYear += 1;
        }
        getPersistenceBrokerTemplate();
        // start with first day of fiscal year and create records for each year up to end date
        GregorianCalendar univPeriodDate = new GregorianCalendar(startDateYear, fiscalYearStartMonth, 1);

        // setup end date
        GregorianCalendar enddate = new GregorianCalendar(univPeriodDate.get(Calendar.YEAR), univPeriodDate.get(Calendar.MONTH), univPeriodDate.get(Calendar.DAY_OF_MONTH));
        enddate.add(Calendar.MONTH, 12);
        enddate.add(Calendar.DAY_OF_MONTH, -1);

        // the fiscal year is always the year of the ending date of the fiscal year
        Integer nextFiscalYear = enddate.get(Calendar.YEAR);

        // get rid of any records already existing for next fiscal year
        // deleteNewYearRows(nextFiscalYear);

        // initialize the period variables
        int period = 1;
        String periodString = String.format("%02d", period);
        int compareMonth = univPeriodDate.get(Calendar.MONTH);
        int currentMonth = compareMonth;

        // loop through the dates until we are past end date
        while (univPeriodDate.compareTo(enddate) <= 0) {
            // if we hit period 13 something went wrong
            if (period == 13) {
                LOG.error("Hit period 13 while creating university date records");
                throw new RuntimeException("Hit period 13 while creating university date records");
            }

            UniversityDate universityDate = null;

            // check if the university date exists and if it does and we are in replace mode then update the record, otherwise create a new one
            Map<String, Date> fields = new HashMap<String, Date>();
            fields.put(KFSPropertyConstants.UNIVERSITY_DATE, new Date(univPeriodDate.getTimeInMillis()));

            int count = businessObjectService.countMatching(UniversityDate.class, fields);
            if (count != 0) {
                if (replaceMode) {
                    universityDate = businessObjectService.findByPrimaryKey(UniversityDate.class, fields);
                    if (ObjectUtils.isNotNull(universityDate)) {
                        if (ObjectUtils.isNotNull(nextFiscalYear) && nextFiscalYear.equals(universityDate.getUniversityFiscalYear()) && StringUtils.isNotEmpty(periodString) && periodString.equals(universityDate.getUniversityFiscalAccountingPeriod())) {
                            // do nothing
                        } else {
                            universityDate.setUniversityFiscalYear(nextFiscalYear);
                            universityDate.setUniversityFiscalAccountingPeriod(periodString);

                            businessObjectService.save(universityDate);
                        }
                    }
                }
            } else {
                // create the university date record
                universityDate = new UniversityDate();
                universityDate.setUniversityDate(new Date(univPeriodDate.getTimeInMillis()));
                universityDate.setUniversityFiscalYear(nextFiscalYear);
                universityDate.setUniversityFiscalAccountingPeriod(periodString);

                businessObjectService.save(universityDate);
            }

            // add one to day for the next record
            univPeriodDate.add(Calendar.DAY_OF_MONTH, 1);

            // does this kick us into a new month and therefore a new accounting period?
            compareMonth = univPeriodDate.get(Calendar.MONTH);
            if (currentMonth != compareMonth) {
                period = period + 1;
                periodString = String.format("%02d", period);
                currentMonth = compareMonth;
            }
        }
    }

    public ParameterService getParameterService() {
        return parameterService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

}
