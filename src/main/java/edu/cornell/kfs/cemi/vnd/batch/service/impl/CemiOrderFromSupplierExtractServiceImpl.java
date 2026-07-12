package edu.cornell.kfs.cemi.vnd.batch.service.impl;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.config.Environment;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.cemi.sys.batch.CemiOutputDefinitionFileType;
import edu.cornell.kfs.cemi.sys.batch.service.CemiFileAppenderService;
import edu.cornell.kfs.cemi.sys.util.CemiUtils;
import edu.cornell.kfs.cemi.vnd.CemiOrderFromSupplierConstants;
import edu.cornell.kfs.cemi.vnd.CemiOrderFromSupplierParameterConstants;
import edu.cornell.kfs.cemi.vnd.batch.CreateCemiOrderFromSupplierExtractStep;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierAddressBo;
import edu.cornell.kfs.cemi.vnd.batch.service.CemiOrderFromSupplierExtractService;
import edu.cornell.kfs.cemi.vnd.dataaccess.CemiOrderFromSupplierDao;
import edu.cornell.kfs.cemi.vnd.dataaccess.CemiOrderFromSupplierOrmDao;
import edu.cornell.kfs.cemi.vnd.dataaccess.CemiVendorOrmDao;
import edu.cornell.kfs.sys.CUKFSConstants;

public class CemiOrderFromSupplierExtractServiceImpl extends CemiDataExtractServiceBase
        implements CemiOrderFromSupplierExtractService {

    private static final Logger LOG = LogManager.getLogger();

    private String reportsDirectory;
    private CemiOrderFromSupplierOrmDao cemiOrderFromSupplierOrmDao;
    private CemiOrderFromSupplierDao cemiOrderFromSupplierDao;
    private CemiVendorOrmDao cemiVendorOrmDao;
    private BusinessObjectService businessObjectService;

    public CemiOrderFromSupplierExtractServiceImpl(final Environment environment) {
        super(environment);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void resetState() {
        LOG.info("resetState, Deleting the temporary address lists from the previous run (if present)...");
        cemiOrderFromSupplierDao.clearExistingListOfKfsVendorAddressLinks();
        cemiOrderFromSupplierDao.clearExistingListOfSupplierAddressLinks();
        cemiOrderFromSupplierDao.clearExistingListOfExtractablePurchaseOrderAddressIds();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void initializeExtractDateSettings() {
        LOG.info("initializeExtractDateSettings, Setting Supplier extraction date-time to use for address queries...");
        final String supplierJobRunDate = getSupplierJobRunDate();
        cemiOrderFromSupplierDao.updateOrderFromSupplierExtractQuerySettings(supplierJobRunDate);
    }

    private String getSupplierJobRunDate() {
        final String supplierJobRunDate = parameterService.getParameterValueAsString(
                CreateCemiOrderFromSupplierExtractStep.class,
                CemiOrderFromSupplierParameterConstants.CEMI_ORDER_FROM_SUPPLIER_EXTRACT_SUPPLIER_DATETIME);
        Validate.validState(StringUtils.isNotBlank(supplierJobRunDate), "Parameter %s should not have been blank",
                CemiOrderFromSupplierParameterConstants.CEMI_ORDER_FROM_SUPPLIER_EXTRACT_SUPPLIER_DATETIME);
        return supplierJobRunDate;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void populateListOfKfsVendorAddressMappings() {
        LOG.info("populateListOfKfsVendorAddressMappings, Populating helper table for mapping KFS Vendor Addresses "
                + "to concatenated address field data...");
        try (
            final Stream<VendorAddress> kfsVendorAddresses = cemiOrderFromSupplierOrmDao
                    .getKfsVendorAddressesForExtractedSuppliers();
        ) {
            final Iterator<VendorAddress> addressIterator = kfsVendorAddresses.iterator();
            cemiOrderFromSupplierDao.storeAsListOfKfsVendorAddressLinks(addressIterator);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void populateListOfSupplierAddressMappings() {
        LOG.info("populateListOfSupplierAddressMappings, Populating helper table for mapping Supplier Addresses "
                + "to concatenated address field data...");
        try (
            final Stream<CemiSupplierAddressBo> supplierAddresses = cemiOrderFromSupplierOrmDao
                    .getSupplierAddressesForExtractedSuppliers();
        ) {
            final Iterator<CemiSupplierAddressBo> addressIterator = supplierAddresses.iterator();
            cemiOrderFromSupplierDao.storeAsListOfSupplierAddressLinks(addressIterator);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void populateListOfInScopeAddresses() {
        LOG.info("populateListOfInScopeAddresses, Querying and storing the list of extractable addresses...");
        cemiOrderFromSupplierDao.queryAndStoreAddressIdsForOrderFromSupplierExtract();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void generateIntermediateOrderFromSupplierExtractData(final LocalDateTime jobRunDate) {
        LOG.info("generateIntermediateOrderFromSupplierExtractData, Generating data rows for Order From Supplier "
                + "spreadsheet and placing in intermediate storage...");
        final String jobRunDateString = CemiUtils.generateBatchJobRunDateAsString(jobRunDate);
        final String skippedSuppliersFilePath = buildPathForSkippedSuppliersReportFile(jobRunDateString);
        try (
            final Stream<CemiSupplierAddressBo> addresses = cemiOrderFromSupplierOrmDao
                    .getSupplierAddressesForOrderFromSupplierExtract();
            final FileOutputStream fileStream = new FileOutputStream(skippedSuppliersFilePath);
            final OutputStreamWriter streamWriter = new OutputStreamWriter(fileStream, StandardCharsets.UTF_8);
            final BufferedWriter skippedSuppliersWriter = new BufferedWriter(streamWriter);
        ) {
            final String supplierJobRunDate = getSupplierJobRunDate();
            final CemiOrderFromSupplierDataBuilderDefaultImpl dataBuilder = new CemiOrderFromSupplierDataBuilderDefaultImpl(
                    businessObjectService, jobRunDateString, supplierJobRunDate, cemiVendorOrmDao,
                    cemiOrderFromSupplierDao, skippedSuppliersWriter, shouldMaskCemiSensitiveData());
            final Iterator<CemiSupplierAddressBo> addressesIterator = addresses.iterator();
            dataBuilder.writeOrderFromSupplierDataToIntermediateStorage(addressesIterator);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String buildPathForSkippedSuppliersReportFile(final String jobRunDate) {
        return StringUtils.join(reportsDirectory,
                CemiOrderFromSupplierConstants.ORDER_FROM_SUPPLIER_SKIPPED_SUPPLIERS_FILE_PREFIX,
                jobRunDate, CUKFSConstants.TEXT_FILE_EXTENSION);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void generateOrderFromSupplierExtractFile(final LocalDateTime jobRunDate) {
        LOG.info("generateOrderFromSupplierExtractFile, Starting creation of CEMI OrderFromSupplier Extract file...");
        generateFileForDataExtract(jobRunDate, CemiOrderFromSupplierConstants.ORDER_FROM_SUPPLIER_EXTRACT_PLAIN_FILENAME,
                CemiOrderFromSupplierConstants.ORDER_FROM_SUPPLIER_EXTRACT_FILENAME_PREFIX);
    }

    @Override
    protected Class<?> getComponentClassForDataMaskingParameter() {
        return CreateCemiOrderFromSupplierExtractStep.class;
    }

    @Override
    protected String getOutputDefinitionFilePathSuffix() {
        return CemiOrderFromSupplierConstants.ORDER_FROM_SUPPLIER_OUTPUT_DEFINITION_PATH_SUFFIX;
    }

    @Override
    protected String getTemplateWorkbookFilePath() {
        return CemiOrderFromSupplierConstants.ORDER_FROM_SUPPLIER_TEMPLATE_FILE_PATH;
    }

    @Override
    protected boolean shouldCopyDataFileToOutboundDirectory() {
        return parameterService.getParameterValueAsBoolean(
                CreateCemiOrderFromSupplierExtractStep.class,
                CemiOrderFromSupplierParameterConstants.COPY_CEMI_ORDER_FROM_SUPPLIER_FILE_TO_OUTBOUND_FOLDER);
    }

    public void setReportsDirectory(final String reportsDirectory) {
        this.reportsDirectory = reportsDirectory;
    }

    public void setCemiOrderFromSupplierOrmDao(final CemiOrderFromSupplierOrmDao cemiOrderFromSupplierOrmDao) {
        this.cemiOrderFromSupplierOrmDao = cemiOrderFromSupplierOrmDao;
    }

    public void setCemiOrderFromSupplierDao(final CemiOrderFromSupplierDao cemiOrderFromSupplierDao) {
        this.cemiOrderFromSupplierDao = cemiOrderFromSupplierDao;
    }

    public void setCemiVendorOrmDao(final CemiVendorOrmDao cemiVendorOrmDao) {
        this.cemiVendorOrmDao = cemiVendorOrmDao;
    }

    public void setCemiFileAppenderService(final CemiFileAppenderService cemiFileAppenderService) {
        this.cemiFileAppenderService = cemiFileAppenderService;
    }

    public void setCemiOutputDefinitionFileType(final CemiOutputDefinitionFileType cemiOutputDefinitionFileType) {
        this.cemiOutputDefinitionFileType = cemiOutputDefinitionFileType;
    }

    public void setBusinessObjectService(final BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

}
