package  edu.cornell.kfs.cemi.vnd.batch.service.impl;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.config.Environment;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.vnd.businessobject.VendorContact;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.cemi.sys.batch.service.impl.CemiDataExtractServiceBase;
import edu.cornell.kfs.cemi.sys.util.CemiUtils;
import edu.cornell.kfs.cemi.vnd.CemiEntityContactConstants;
import edu.cornell.kfs.cemi.vnd.CemiEntityContactParameterConstants;
import edu.cornell.kfs.cemi.vnd.batch.CreateCemiEntityContactExtractStep;
import edu.cornell.kfs.cemi.vnd.batch.service.CemiEntityContactExtractService;
import edu.cornell.kfs.cemi.vnd.dataaccess.CemiEntityContactExtractDao;
import edu.cornell.kfs.cemi.vnd.dataaccess.CemiEntityContactExtractOrmDao;

public class CemiEntityContactExtractServiceImpl extends CemiDataExtractServiceBase
        implements CemiEntityContactExtractService {

    private static final Logger LOG = LogManager.getLogger();

    private CemiEntityContactExtractOrmDao cemiEntityContactExtractOrmDao;
    private CemiEntityContactExtractDao cemiEntityContactExtractDao;
    private BusinessObjectService businessObjectService;

    public CemiEntityContactExtractServiceImpl(final Environment environment) {
        super(environment);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void resetState() {
        LOG.info("resetState, Deleting the list of keys representing extractable Vendor Contacts from"
                + " the previous run (if present)...");
        cemiEntityContactExtractDao.clearAnyExistingInScopeVendorContactKeysFromPreviousExecution();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void captureInScopeBusinessObjectKeysToProcessingTable() {
        initializeExtractDateSettings();
        LOG.info("captureInScopeBusinessObjectKeysToProcessingTable, Querying and storing the list of keys "
                + "representing the extractable Vendor Contacts...");
        cemiEntityContactExtractDao.queryAndStoreInScopeVendorContactKeysForDataExtract();
    }

    private void initializeExtractDateSettings() {
        LOG.info("initializeExtractDateSettings, Setting Supplier extraction date-time to use for Vendor Contact queries...");
        final String supplierJobRunDateString = getSupplierJobRunDateString();
        cemiEntityContactExtractDao.updateEntityContactExtractQuerySettings(supplierJobRunDateString);
    }

    private String getSupplierJobRunDateString() {
        final String supplierJobRunDateString = parameterService.getParameterValueAsString(
                CreateCemiEntityContactExtractStep.class,
                CemiEntityContactParameterConstants.CEMI_ENTITY_CONTACT_EXTRACT_SUPPLIER_DATETIME);
        Validate.validState(StringUtils.isNotBlank(supplierJobRunDateString), "Parameter %s should not have been blank",
                CemiEntityContactParameterConstants.CEMI_ENTITY_CONTACT_EXTRACT_SUPPLIER_DATETIME);
        return supplierJobRunDateString;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void generateIntermediateExtractData(final LocalDateTime jobRunDate) {
        LOG.info("generateIntermediateExtractData, Generating data rows for {} spreadsheet and placing in "
                + "intermediate storage...", CemiEntityContactConstants.ENTITY_CONTACT_EXTRACT_PLAIN_FILENAME);

        try (
                final Stream<VendorContact> vendorContacts = 
                        cemiEntityContactExtractOrmDao.getVendorContactsForCemiEntityContactExtractAsCloseableStream();
        ) {
            final String jobRunDateString = CemiUtils.generateBatchJobRunDateAsString(jobRunDate);
            final String supplierJobRunDateString = getSupplierJobRunDateString();
            final CemiEntityContactFileExtractDataBuilderDefaultImpl dataBuilder = new CemiEntityContactFileExtractDataBuilderDefaultImpl(
                    businessObjectService, jobRunDateString, supplierJobRunDateString,
                    cemiEntityContactExtractDao, shouldMaskCemiSensitiveData());
            final Iterator<VendorContact> vendorContactIterator = vendorContacts.iterator();
            dataBuilder.writeEntityContactFileEntityContactTabExtractDataToIntermediateStorage(vendorContactIterator);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void generateDataConversionExtractFile(final LocalDateTime jobRunDate) {
        LOG.info("generateEntityContactExtractFile, Starting creation of CEMI Extract file {}",
                CemiEntityContactConstants.ENTITY_CONTACT_EXTRACT_PLAIN_FILENAME);
        generateFileForDataExtract(jobRunDate, CemiEntityContactConstants.ENTITY_CONTACT_EXTRACT_PLAIN_FILENAME,
                CemiEntityContactConstants.ENTITY_CONTACT_EXTRACT_FILENAME_PREFIX);
    }

    @Override
    protected Class<?> getComponentClassForDataExtractParameter() {
        return CreateCemiEntityContactExtractStep.class;
    }

    @Override
    protected String getOutputDefinitionFilePathSuffix() {
        return CemiEntityContactConstants.ENTITY_CONTACT_OUTPUT_DEFINITION_PATH_SUFFIX;
    }

    @Override
    protected String getTemplateWorkbookFilePathSuffix() {
        return CemiEntityContactConstants.ENTITY_CONTACT_TEMPLATE_WORKBOOK_FILE_PATH_SUFFIX;
    }

    public void setCemiEntityContactExtractOrmDao(final CemiEntityContactExtractOrmDao cemiEntityContactExtractOrmDao) {
        this.cemiEntityContactExtractOrmDao = cemiEntityContactExtractOrmDao;
    }

    public void setCemiEntityContactExtractDao(final CemiEntityContactExtractDao cemiEntityContactExtractDao) {
        this.cemiEntityContactExtractDao = cemiEntityContactExtractDao;
    }

    public void setBusinessObjectService(final BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

}
