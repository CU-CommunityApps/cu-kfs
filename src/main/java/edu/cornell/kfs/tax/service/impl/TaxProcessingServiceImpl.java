package edu.cornell.kfs.tax.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.CoreApiServiceLocator;
import org.kuali.kfs.core.api.config.property.ConfigContext;
import org.kuali.kfs.core.api.criteria.CriteriaLookupService;
import org.kuali.kfs.core.api.criteria.PredicateFactory;
import org.kuali.kfs.core.api.criteria.QueryByCriteria;
import org.kuali.kfs.core.api.util.CoreUtilities;
import org.kuali.kfs.coreservice.framework.CoreFrameworkServiceLocator;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.core.api.util.CuCoreUtilities;
import edu.cornell.kfs.sys.batch.service.DigestorXMLBatchInputFileType;
import edu.cornell.kfs.tax.CUTaxConstants;
import edu.cornell.kfs.tax.CUTaxConstants.CUTaxKeyConstants;
import edu.cornell.kfs.tax.CUTaxConstants.TaxCommonParameterNames;
import edu.cornell.kfs.tax.batch.TaxDataDefinition;
import edu.cornell.kfs.tax.batch.TaxDataDefinitionFileType;
import edu.cornell.kfs.tax.batch.TaxOutputDefinition;
import edu.cornell.kfs.tax.batch.TaxOutputDefinitionFileType;
import edu.cornell.kfs.tax.businessobject.ObjectCodeBucketMapping;
import edu.cornell.kfs.tax.businessobject.TransactionOverride;
import edu.cornell.kfs.tax.dataaccess.TaxProcessingDao;
import edu.cornell.kfs.tax.service.TaxProcessingService;

/**
 * Default implementation of TaxProcessingService.
 */
public class TaxProcessingServiceImpl implements TaxProcessingService {
	private static final Logger LOG = LogManager.getLogger(TaxProcessingServiceImpl.class);

    private static final String TAX_TYPE_FIELD = "taxType";
    private static final String BOX_NUMBER_FIELD = "boxNumber";
    private static final int DAY_31 = 31;

    private TaxOutputDefinitionFileType taxOutputDefinitionFileType;
    private TaxDataDefinitionFileType taxDataDefinitionFileType;
    private TaxProcessingDao taxProcessingDao;

    @Override
    @Transactional
    public void doTaxProcessing(String taxType, java.util.Date processingStartDate) {
        if (processingStartDate == null) {
            throw new IllegalArgumentException("processingStartDate cannot be null");
        }
        ParameterService parameterService = CoreFrameworkServiceLocator.getParameterService();
        String taxDetailType;
        int reportYear;
        java.sql.Date startDate;
        java.sql.Date endDate;
        boolean vendorForeign;
        
        
        
        /*
         * Perform basic tax-type-specific setup.
         */
        if (CUTaxConstants.TAX_TYPE_1099.equals(taxType)) {
            // Do 1099 tax processing.
            taxDetailType = CUTaxConstants.TAX_1099_PARM_DETAIL;
            vendorForeign = false;
            
        } else if (CUTaxConstants.TAX_TYPE_1042S.equals(taxType)) {
            // Do 1042S tax processing.
            taxDetailType = CUTaxConstants.TAX_1042S_PARM_DETAIL;
            vendorForeign = true;
            
        } else {
            throw new IllegalArgumentException("Invalid tax reporting type");
        }
        
        
        
        /*
         * Determine report year, start date, and end date.
         */
        Collection<String> datesToProcess = parameterService.getParameterValuesAsString(
                CUTaxConstants.TAX_NAMESPACE, taxDetailType, taxType + TaxCommonParameterNames.DATES_TO_PROCESS_PARAMETER_SUFFIX);
        Calendar tempCalendar = CoreApiServiceLocator.getDateTimeService().getCurrentCalendar();
        
        if (datesToProcess.isEmpty()) {
            throw new IllegalStateException("Dates-to-process parameter cannot be empty or absent");
        } else if (datesToProcess.size() == 1) {
            // If single value, then it should equal a specific year (or a shortcut value representing a recent year).
            String yearValue = datesToProcess.iterator().next();
            
            if (CUTaxConstants.YEAR_TO_DATE.equals(yearValue)) {
                // Current tax year; set report year to current year and end date to current date.
                reportYear = tempCalendar.get(Calendar.YEAR);
                endDate = new java.sql.Date(tempCalendar.getTime().getTime());
                
            } else if (CUTaxConstants.PREVIOUS_YEAR_TO_DATE.equals(yearValue)) {
                // Previous tax year; set report year to previous year and end date to December 31 of previous year.
                tempCalendar.set(tempCalendar.get(Calendar.YEAR) - 1, Calendar.DECEMBER, DAY_31);
                reportYear = tempCalendar.get(Calendar.YEAR);
                endDate = new java.sql.Date(tempCalendar.getTime().getTime());
                
            } else {
                // Specific year; set report year to given year and end date to current date or December 31 of given year, depending on current year.
                try {
                    reportYear = Integer.parseInt(yearValue);
                } catch (NumberFormatException e) {
                    throw new IllegalStateException("Dates-to-process parameter's literal year value was not an integer");
                }
                if (reportYear != tempCalendar.get(Calendar.YEAR)) {
                    tempCalendar.set(reportYear, Calendar.DECEMBER, DAY_31);
                }
                endDate = new java.sql.Date(tempCalendar.getTime().getTime());
                
            }
            
            // In all year-only cases, set start date to January 1 of given year.
            tempCalendar.set(reportYear, Calendar.JANUARY, 1);
            startDate = new java.sql.Date(tempCalendar.getTime().getTime());
            
        } else if (datesToProcess.size() == 2) {
            // If two values, then they should be start dates and end dates in the same year.
            String[] dateValues = datesToProcess.toArray(new String[2]);
            try {
                startDate = CoreApiServiceLocator.getDateTimeService().convertToSqlDate(dateValues[0]);
                endDate = CoreApiServiceLocator.getDateTimeService().convertToSqlDate(dateValues[1]);
            } catch (ParseException e) {
                throw new IllegalStateException("Dates-to-process parameter contains one or more invalid date values");
            }
            
            // Set report year to the year of the starting date.
            tempCalendar.setTimeInMillis(startDate.getTime());
            reportYear = tempCalendar.get(Calendar.YEAR);
            // Make sure the dates are in the same year, and start date is earlier than or equal to end date.
            tempCalendar.setTimeInMillis(endDate.getTime());
            if (reportYear != tempCalendar.get(Calendar.YEAR)) {
                throw new IllegalStateException("Dates-to-process parameter's start and end dates are not in the same year");
            } else if (startDate.compareTo(endDate) > 0) {
                throw new IllegalStateException("Dates-to-process parameter's start date cannot be later than its end date");
            }
            
        } else {
            throw new IllegalStateException("Dates-to-process parameter cannot have more than two values");
        }
        
        
        
        /*
         * Perform the main processing.
         */
        LOG.info("==== Start of tax processing ====");
        LOG.info("Performing " + taxType + " tax processing for the given time period:");
        LOG.info("Report Year: " + Integer.toString(reportYear) + ", Start Date: " + startDate.toString() + ", End Date: " + endDate.toString());
        
        taxProcessingDao.doTaxProcessing(taxType, reportYear, startDate, endDate, vendorForeign, processingStartDate);
        
        LOG.info("==== End of tax processing ====");
    }



    @Override
    public TaxOutputDefinition getOutputDefinition(String taxParamPrefix, int reportYear) {
        return getXMLBasedDefinition(taxParamPrefix, reportYear, TaxOutputDefinition.class, taxOutputDefinitionFileType);
    }

    @Override
    public TaxDataDefinition getDataDefinition(String taxParamPrefix, int reportYear) {
        return getXMLBasedDefinition(taxParamPrefix, reportYear, TaxDataDefinition.class, taxDataDefinitionFileType);
    }

    /*
     * Helper method for parsing TaxOutputDefinition or TaxDataDefinition objects from XML.
     */
    private <E> E getXMLBasedDefinition(String taxParamPrefix, int reportYear, Class<E> definitionClass, DigestorXMLBatchInputFileType xmlFileType) {
        InputStream definitionStream = null;
        byte[] definitionContent;
    
        // Get definition from year-specific file or default file.
        String definitionFilePath = ConfigContext.getCurrentContextConfig().getProperty(taxParamPrefix + Integer.toString(reportYear));
        if (StringUtils.isBlank(definitionFilePath)) {
            definitionFilePath = ConfigContext.getCurrentContextConfig().getProperty(taxParamPrefix + CUTaxKeyConstants.TAX_CONFIG_DEFAULT_SUFFIX);
        }
        if (StringUtils.isBlank(definitionFilePath)) {
            throw new IllegalStateException("No default or given-year definition file path found");
        }
        
        // Parse the definition from the file, in a manner similar to our CU VendorBatchServiceImpl.safelyLoadFileBytes() method.
        try {
            definitionStream = CuCoreUtilities.getResourceAsStream(definitionFilePath);
            definitionContent = IOUtils.toByteArray(definitionStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (definitionStream != null) {
                try {
                    definitionStream.close();
                } catch (IOException e) {
                    LOG.error("Could not close tax definition file input");
                }
            }
        }
        
        return definitionClass.cast(xmlFileType.parse(definitionContent));
    }



    /**
     * This implementation only supports 1099 bucket mappings.
     * 
     * @see edu.cornell.kfs.tax.service.TaxProcessingService#getBucketMappings(java.lang.String)
     */
    @Override
    public List<ObjectCodeBucketMapping> getBucketMappings(String taxType) {
        if (StringUtils.isBlank(taxType)) {
            throw new IllegalArgumentException("Bucket mapping taxType cannot be blank");
        } else if (!CUTaxConstants.TAX_TYPE_1099.equals(taxType)) {
            throw new IllegalArgumentException("Cannot retrieve bucket mappings for tax types other than 1099");
        }
        
        return SpringContext.getBean(CriteriaLookupService.class).lookup(ObjectCodeBucketMapping.class, QueryByCriteria.Builder.fromPredicates(
                PredicateFactory.isNotNull(BOX_NUMBER_FIELD),
                PredicateFactory.equal(KFSPropertyConstants.ACTIVE, KFSConstants.ACTIVE_INDICATOR)
        )).getResults();
    }

    @Override
    public List<TransactionOverride> getTransactionOverrides(String taxType, java.sql.Date startDate, java.sql.Date endDate) {
        if (StringUtils.isBlank(taxType)) {
            throw new IllegalArgumentException("Transaction override taxType cannot be blank");
        } else if (startDate == null) {
            throw new IllegalArgumentException("startDate cannot be null");
        } else if (endDate == null) {
            throw new IllegalArgumentException("endDate cannot be null");
        }
        
        return SpringContext.getBean(CriteriaLookupService.class).lookup(TransactionOverride.class, QueryByCriteria.Builder.fromPredicates(
                PredicateFactory.equal(TAX_TYPE_FIELD, taxType),
                PredicateFactory.greaterThanOrEqual(KFSPropertyConstants.UNIVERSITY_DATE, startDate),
                PredicateFactory.lessThanOrEqual(KFSPropertyConstants.UNIVERSITY_DATE, endDate),
                PredicateFactory.equal(KFSPropertyConstants.ACTIVE, KFSConstants.ACTIVE_INDICATOR)
        )).getResults();
    }



    public void setTaxOutputDefinitionFileType(TaxOutputDefinitionFileType taxOutputDefinitionFileType) {
        this.taxOutputDefinitionFileType = taxOutputDefinitionFileType;
    }

    public void setTaxDataDefinitionFileType(TaxDataDefinitionFileType taxDataDefinitionFileType) {
        this.taxDataDefinitionFileType = taxDataDefinitionFileType;
    }

    public void setTaxProcessingDao(TaxProcessingDao taxProcessingDao) {
        this.taxProcessingDao = taxProcessingDao;
    }

}
