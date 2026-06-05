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
import edu.cornell.kfs.cemi.vnd.CemiOrderFromSupplierParameterConstants;
import edu.cornell.kfs.cemi.vnd.batch.CreateCemiRemitToSupplierExtractStep;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierAddressBo;
import edu.cornell.kfs.cemi.vnd.batch.service.CemiOrderFromSupplierExtractService;
import edu.cornell.kfs.cemi.vnd.dataaccess.CemiOrderFromSupplierDao;
import edu.cornell.kfs.cemi.vnd.dataaccess.CemiOrderFromSupplierOrmDao;

public class CemiOrderFromSupplierExtractServiceImpl implements CemiOrderFromSupplierExtractService {

    private static final Logger LOG = LogManager.getLogger();

    private final Environment environment;
    private String orderFromSupplierFileCreationDirectory;
    private String orderFromSupplierFileOutboundDirectory;
    private CemiOrderFromSupplierOrmDao cemiOrderFromSupplierOrmDao;
    private CemiOrderFromSupplierDao cemiOrderFromSupplierDao;
    private CemiFileAppenderService cemiFileAppenderService;
    private CemiOutputDefinitionFileType cemiOutputDefinitionFileType;
    private BusinessObjectService businessObjectService;
    private ParameterService parameterService;

    public CemiOrderFromSupplierExtractServiceImpl(final Environment environment) {
        this.environment = environment;
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
                CreateCemiRemitToSupplierExtractStep.class,
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
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void generateOrderFromSupplierExtractFile(final LocalDateTime jobRunDate) {
        
    }

    public void setOrderFromSupplierFileCreationDirectory(final String orderFromSupplierFileCreationDirectory) {
        this.orderFromSupplierFileCreationDirectory = orderFromSupplierFileCreationDirectory;
    }

    public void setOrderFromSupplierFileOutboundDirectory(final String orderFromSupplierFileOutboundDirectory) {
        this.orderFromSupplierFileOutboundDirectory = orderFromSupplierFileOutboundDirectory;
    }

    public void setCemiOrderFromSupplierOrmDao(final CemiOrderFromSupplierOrmDao cemiOrderFromSupplierOrmDao) {
        this.cemiOrderFromSupplierOrmDao = cemiOrderFromSupplierOrmDao;
    }

    public void setCemiOrderFromSupplierDao(final CemiOrderFromSupplierDao cemiOrderFromSupplierDao) {
        this.cemiOrderFromSupplierDao = cemiOrderFromSupplierDao;
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
