package edu.cornell.kfs.cemi.vnd.batch.service.impl;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.config.Environment;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.cemi.sys.batch.CemiOutputDefinitionFileType;
import edu.cornell.kfs.cemi.sys.batch.service.CemiFileAppenderService;
import edu.cornell.kfs.cemi.vnd.CemiRemitToSupplierParameterConstants;
import edu.cornell.kfs.cemi.vnd.batch.CreateCemiRemitToSupplierExtractStep;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierAddressBo;
import edu.cornell.kfs.cemi.vnd.batch.service.CemiSupplierOrderFromExtractService;
import edu.cornell.kfs.cemi.vnd.dataaccess.CemiSupplierOrderFromDao;
import edu.cornell.kfs.cemi.vnd.dataaccess.CemiSupplierOrderFromOrmDao;

public class CemiSupplierOrderFromExtractServiceImpl implements CemiSupplierOrderFromExtractService {

    private static final Logger LOG = LogManager.getLogger();

    private final Environment environment;
    private String remitToSupplierFileCreationDirectory;
    private String remitToSupplierFileOutboundDirectory;
    private CemiSupplierOrderFromOrmDao cemiSupplierOrderFromOrmDao;
    private CemiSupplierOrderFromDao cemiSupplierOrderFromDao;
    private CemiFileAppenderService cemiFileAppenderService;
    private CemiOutputDefinitionFileType cemiOutputDefinitionFileType;
    private BusinessObjectService businessObjectService;
    private ParameterService parameterService;

    public CemiSupplierOrderFromExtractServiceImpl(final Environment environment) {
        this.environment = environment;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void resetState() {
        LOG.info("resetState, Deleting the temporary address lists from the previous run (if present)...");
        cemiSupplierOrderFromDao.clearExistingListOfKfsVendorAddressLinks();
        cemiSupplierOrderFromDao.clearExistingListOfSupplierAddressLinks();
        cemiSupplierOrderFromDao.clearExistingListOfExtractablePurchaseOrderAddressIds();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void initializeExtractDateSettings() {
        LOG.info("initializeExtractDateSettings, Setting Supplier extraction date-time to use for address queries...");
        final String supplierJobRunDate = getSupplierJobRunDate();
        cemiSupplierOrderFromDao.updateSupplierOrderFromExtractQuerySettings(supplierJobRunDate);
    }

    private String getSupplierJobRunDate() {
        final String supplierJobRunDate = parameterService.getParameterValueAsString(
                CreateCemiRemitToSupplierExtractStep.class,
                CemiRemitToSupplierParameterConstants.CEMI_SUPPLIER_ORDER_FROM_EXTRACT_SUPPLIER_DATETIME);
        Validate.validState(StringUtils.isNotBlank(supplierJobRunDate), "Parameter %s should not have been blank",
                CemiRemitToSupplierParameterConstants.CEMI_SUPPLIER_ORDER_FROM_EXTRACT_SUPPLIER_DATETIME);
        return supplierJobRunDate;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void populateListOfKfsVendorAddressMappings() {
        LOG.info("populateListOfKfsVendorAddressMappings, Populating helper table for mapping KFS Vendor Addresses "
                + "to concatenated address field data...");
        try (
            final Stream<VendorAddress> kfsVendorAddresses = cemiSupplierOrderFromOrmDao
                    .getKfsVendorAddressesForExtractedSuppliers();
        ) {
            final Iterator<VendorAddress> addressIterator = kfsVendorAddresses.iterator();
            cemiSupplierOrderFromDao.storeAsListOfKfsVendorAddressLinks(addressIterator);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void populateListOfSupplierAddressMappings() {
        LOG.info("populateListOfSupplierAddressMappings, Populating helper table for mapping Supplier Addresses "
                + "to concatenated address field data...");
        try (
            final Stream<CemiSupplierAddressBo> supplierAddresses = cemiSupplierOrderFromOrmDao
                    .getSupplierAddressesForExtractedSuppliers();
        ) {
            final Iterator<CemiSupplierAddressBo> addressIterator = supplierAddresses.iterator();
            cemiSupplierOrderFromDao.storeAsListOfSupplierAddressLinks(addressIterator);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void populateListOfInScopeAddresses() {
        LOG.info("populateListOfInScopeAddresses, Querying and storing the list of extractable addresses...");
        cemiSupplierOrderFromDao.queryAndStoreAddressIdsForSupplierOrderFromExtract();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void generateIntermediateSupplierOrderFromExtractData(final LocalDateTime jobRunDate) {
        LOG.info("generateIntermediateSupplierOrderFromExtractData, Generating data rows for Supplier Order From "
                + "spreadsheet and placing in intermediate storage...");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void generateSupplierOrderFromExtractFile(final LocalDateTime jobRunDate) {
        
    }

    public void setRemitToSupplierFileCreationDirectory(final String remitToSupplierFileCreationDirectory) {
        this.remitToSupplierFileCreationDirectory = remitToSupplierFileCreationDirectory;
    }

    public void setRemitToSupplierFileOutboundDirectory(final String remitToSupplierFileOutboundDirectory) {
        this.remitToSupplierFileOutboundDirectory = remitToSupplierFileOutboundDirectory;
    }

    public void setCemiSupplierOrderFromOrmDao(final CemiSupplierOrderFromOrmDao cemiSupplierOrderFromOrmDao) {
        this.cemiSupplierOrderFromOrmDao = cemiSupplierOrderFromOrmDao;
    }

    public void setCemiSupplierOrderFromDao(final CemiSupplierOrderFromDao cemiSupplierOrderFromDao) {
        this.cemiSupplierOrderFromDao = cemiSupplierOrderFromDao;
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

    public void setParameterService(final ParameterService parameterService) {
        this.parameterService = parameterService;
    }

}
