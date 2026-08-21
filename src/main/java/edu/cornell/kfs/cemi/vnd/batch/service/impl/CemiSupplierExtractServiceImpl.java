package edu.cornell.kfs.cemi.vnd.batch.service.impl;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Stream;

import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.config.Environment;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.cemi.sys.batch.CemiOutputDefinitionFileType;
import edu.cornell.kfs.cemi.sys.batch.service.impl.CemiDataExtractServiceBase;
import edu.cornell.kfs.cemi.sys.util.CemiUtils;
import edu.cornell.kfs.cemi.vnd.CemiSupplierConstants;
import edu.cornell.kfs.cemi.vnd.CemiSupplierParameterConstants;
import edu.cornell.kfs.cemi.vnd.batch.CreateCemiSupplierExtractStep;
import edu.cornell.kfs.cemi.vnd.batch.service.CemiSupplierExtractService;
import edu.cornell.kfs.cemi.vnd.dataaccess.CemiVendorDao;
import edu.cornell.kfs.cemi.vnd.dataaccess.CemiVendorOrmDao;
import edu.cornell.kfs.sys.service.ISOFIPSConversionService;

public class CemiSupplierExtractServiceImpl extends CemiDataExtractServiceBase implements CemiSupplierExtractService {

    private static final Logger LOG = LogManager.getLogger();

    private CemiVendorOrmDao cemiVendorOrmDao;
    private CemiVendorDao cemiVendorDao;
    private BusinessObjectService businessObjectService;
    private ParameterService parameterService;
    private DateTimeService dateTimeService;
    private ISOFIPSConversionService isoFipsConversionService;

    public CemiSupplierExtractServiceImpl(final Environment environment) {
        super(environment);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void resetState() {
        LOG.info("resetState, Deleting the list of extractable Vendors from the previous run (if present)...");
        getCemiVendorDao().clearExistingListOfBaseVendorData();
        getCemiVendorDao().clearExistingListOfExtractableVendorIds();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void captureInScopeBusinessObjectKeysToProcessingTable() {
        initializeVendorActivityDateRangeSettings();
        populateListOfBaseVendorData();
        populateListOfInScopeVendors();
    }
    
    private void initializeVendorActivityDateRangeSettings() {
        LOG.info("initializeVendorActivityDateRangeSettings, Setting from/to date range from parameter value...");
        final Collection<String> parameterValues = parameterService.getParameterValuesAsString(
                CreateCemiSupplierExtractStep.class, CemiSupplierParameterConstants.CEMI_SUPPLIER_EXTRACT_DATE_RANGE);
        final String[] dateStrings = parameterValues.toArray(String[]::new);
        Validate.validState(dateStrings.length == 2, "Parameter %s should have had 2 values, but had %s instead",
                CemiSupplierParameterConstants.CEMI_SUPPLIER_EXTRACT_DATE_RANGE, dateStrings.length);

        final LocalDate fromDate = parseDate(dateStrings[0]);
        final LocalDate toDate = parseDate(dateStrings[1]);
        Validate.validState(fromDate.compareTo(toDate) <= 0,
                "Parameter %s contained a 'from' date that is later than the 'to' date",
                CemiSupplierParameterConstants.CEMI_SUPPLIER_EXTRACT_DATE_RANGE);

        getCemiVendorDao().updateSupplierExtractQuerySettings(fromDate, toDate);
    }

    private LocalDate parseDate(final String value) {
        try {
            return dateTimeService.convertToLocalDate(value);
        } catch (final ParseException e) {
            LOG.error("parseDate, failed to parse date string: {}", value, e);
            throw new RuntimeException(e);
        }
    }

    private void populateListOfBaseVendorData() {
        LOG.info("populateListOfBaseVendorData, Preparing base Vendor data needed for subsequent Vendor query...");
        getCemiVendorDao().prepareBaseVendorDataNeededForMainVendorIdQuery();
    }

    private void populateListOfInScopeVendors() {
        LOG.info("populateListOfInScopeVendors, Querying and storing the list of extractable Vendors...");
        getCemiVendorDao().queryAndStoreVendorIdsForSupplierExtract();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void generateIntermediateExtractData(LocalDateTime jobRunDate) {
        LOG.info("generateIntermediateExtractData, Generating data rows for {} spreadsheet and placing in "
                + "intermediate storage...", CemiSupplierConstants.SUPPLIER_EXTRACT_PLAIN_FILENAME);
        try (
                final Stream<VendorDetail> vendors = getCemiVendorOrmDao()
                        .getVendorsForCemiSupplierExtractAsCloseableStream();
        ) {
            final String jobRunDateString = CemiUtils.generateBatchJobRunDateAsString(jobRunDate);
            final CemiSupplierFileExtractDataBuilderDefaultImpl dataBuilder = new CemiSupplierFileExtractDataBuilderDefaultImpl(
                    businessObjectService, jobRunDateString, isoFipsConversionService,
                    shouldMaskCemiSensitiveData());
            final Iterator<VendorDetail> vendorsIterator = vendors.iterator();
            dataBuilder.writeSupplierFileExtractDataForAllMappedTabsToIntermediateStorage(vendorsIterator);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void generateDataConversionExtractFile(final LocalDateTime jobRunDate) {
        LOG.info("generateEntityContactExtractFile, Starting creation of CEMI Extract file {}",
                CemiSupplierConstants.SUPPLIER_EXTRACT_PLAIN_FILENAME);
        generateFileForDataExtract(jobRunDate, CemiSupplierConstants.SUPPLIER_EXTRACT_PLAIN_FILENAME,
                CemiSupplierConstants.SUPPLIER_EXTRACT_FILENAME_PREFIX);
    }

    @Override
    protected Class<?> getComponentClassForDataExtractParameter() {
        return CreateCemiSupplierExtractStep.class;
    }

    @Override
    protected String getOutputDefinitionFilePathSuffix() {
        return CemiSupplierConstants.SUPPLIER_OUTPUT_DEFINITION_FILE_PATH_SUFFIX;
    }

    @Override
    protected String getTemplateWorkbookFilePathSuffix() {
        return CemiSupplierConstants.SUPPLIER_TEMPLATE_WORKBOOK_FILE_PATH_SUFFIX;
    }

    public void setCemiOutputDefinitionFileType(final CemiOutputDefinitionFileType cemiOutputDefinitionFileType) {
        this.cemiOutputDefinitionFileType = cemiOutputDefinitionFileType;
    }

    public void setBusinessObjectService(final BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public void setParameterService(final ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public void setDateTimeService(final DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    public CemiVendorDao getCemiVendorDao() {
        return cemiVendorDao;
    }

    public void setCemiVendorDao(CemiVendorDao cemiVendorDao) {
        this.cemiVendorDao = cemiVendorDao;
    }

    public CemiVendorOrmDao getCemiVendorOrmDao() {
        return cemiVendorOrmDao;
    }

    public void setCemiVendorOrmDao(CemiVendorOrmDao cemiVendorOrmDao) {
        this.cemiVendorOrmDao = cemiVendorOrmDao;
    }

    public void setIsoFipsConversionService(final ISOFIPSConversionService isoFipsConversionService) {
        this.isoFipsConversionService = isoFipsConversionService;
    }

}
