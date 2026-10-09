package  edu.cornell.kfs.cemi.module.purap.batch.service.impl;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.config.Environment;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.cemi.module.purap.CemiPurchaseOrderConstants;
import edu.cornell.kfs.cemi.module.purap.CemiPurchaseOrderParameterConstants;
import edu.cornell.kfs.cemi.module.purap.batch.CreateCemiPurchaseOrderExtractStep;
import edu.cornell.kfs.cemi.module.purap.batch.businessobject.CemiLegacyPurchaseOrder;
import edu.cornell.kfs.cemi.module.purap.batch.businessobject.CemiPurchaseOrderExtractRow;
import edu.cornell.kfs.cemi.module.purap.batch.service.CemiPurchaseOrderExtractService;
import edu.cornell.kfs.cemi.module.purap.batch.service.CemiPurchaseOrderFileExtractDataBuilder;
import edu.cornell.kfs.cemi.module.purap.dataaccess.CemiPurchaseOrderExtractDao;
import edu.cornell.kfs.cemi.sys.batch.service.impl.CemiDataExtractServiceBase;
import edu.cornell.kfs.cemi.sys.util.CemiUtils;

public class CemiPurchaseOrderExtractServiceImpl extends CemiDataExtractServiceBase
        implements CemiPurchaseOrderExtractService {

    private static final Logger LOG = LogManager.getLogger();

    private CemiPurchaseOrderExtractDao cemiPurchaseOrderExtractDao;
    private BusinessObjectService businessObjectService;
    private PersonService personService;

    public CemiPurchaseOrderExtractServiceImpl(final Environment environment) {
        super(environment);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void resetState() {
        LOG.info("resetState, Deleting the list of keys representing extractable Purchase Orders from"
                + " the previous run (if present)...");
        cemiPurchaseOrderExtractDao.clearAnyExistingInScopeBusinessObjectKeysFromPreviousExecution();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void captureInScopeBusinessObjectKeysToProcessingTable() {
        LOG.info("captureInScopeBusinessObjectKeysToProcessingTable, Querying and storing the list of keys "
                + "representing the extractable Purchase Orders...");
        cemiPurchaseOrderExtractDao.queryAndStoreInScopeBusinessObjectKeysForDataExtract();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void generateIntermediateExtractData(final LocalDateTime jobRunDate) {
        LOG.info("generateIntermediateExtractData, Generating data rows for {} spreadsheet and placing in "
                + "intermediate storage...", CemiPurchaseOrderConstants.PURCHASE_ORDER_EXTRACT_PLAIN_FILENAME);

        final String supplierJobRunDateString = getSupplierJobRunDateString();
        try (
                final Stream<CemiPurchaseOrderExtractRow> purchaseOrderExtractRows =
                        cemiPurchaseOrderExtractDao.getPurchaseOrderExtractRowsAsCloseableStream(
                                supplierJobRunDateString);
        ) {
            final String jobRunDateString = CemiUtils.generateBatchJobRunDateAsString(jobRunDate);
            final CemiPurchaseOrderFileExtractDataBuilder dataBuilder = new CemiPurchaseOrderFileExtractDataBuilderDefaultImpl(
                    businessObjectService, jobRunDateString, personService, parameterService,
                    supplierJobRunDateString, shouldMaskCemiSensitiveData());
            final Iterator<CemiLegacyPurchaseOrder> purchaseOrdersIterator = new CemiPurchaseOrderIterator(
                    purchaseOrderExtractRows.iterator());
            dataBuilder.writePurchaseOrderFileSubmitPurchaseOrderTabExtractDataToIntermediateStorage(purchaseOrdersIterator);
        }
    }

    private String getSupplierJobRunDateString() {
        final String supplierJobRunDateString = parameterService.getParameterValueAsString(
                CreateCemiPurchaseOrderExtractStep.class,
                CemiPurchaseOrderParameterConstants.CEMI_PURCHASE_ORDER_EXTRACT_SUPPLIER_DATETIME);
        Validate.validState(StringUtils.isNotBlank(supplierJobRunDateString), "Parameter %s should not have been blank",
                CemiPurchaseOrderParameterConstants.CEMI_PURCHASE_ORDER_EXTRACT_SUPPLIER_DATETIME);
        return supplierJobRunDateString;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void generateDataConversionExtractFile(final LocalDateTime jobRunDate) {
        LOG.info("generatePurchaseOrderrExtractFile, Starting creation of CEMI Extract file {}",
                CemiPurchaseOrderConstants.PURCHASE_ORDER_EXTRACT_PLAIN_FILENAME);
        generateFileForDataExtract(jobRunDate, CemiPurchaseOrderConstants.PURCHASE_ORDER_EXTRACT_PLAIN_FILENAME,
                CemiPurchaseOrderConstants.PURCHASE_ORDER_EXTRACT_FILENAME_PREFIX);
    }

    @Override
    protected Class<?> getComponentClassForDataExtractParameter() {
        return CreateCemiPurchaseOrderExtractStep.class;
    }

    @Override
    protected String getOutputDefinitionFilePathSuffix() {
        return CemiPurchaseOrderConstants.PURCHASE_ORDER_OUTPUT_DEFINITION_PATH_SUFFIX;
    }

    @Override
    protected String getTemplateWorkbookFilePathSuffix() {
        return CemiPurchaseOrderConstants.PURCHASE_ORDER_TEMPLATE_WORKBOOK_FILE_PATH_SUFFIX;
    }

    public void setCemiPurchaseOrderExtractDao(final CemiPurchaseOrderExtractDao cemiPurchaseOrderExtractDao) {
        this.cemiPurchaseOrderExtractDao = cemiPurchaseOrderExtractDao;
    }

    public void setBusinessObjectService(final BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public void setPersonService(final PersonService personService) {
        this.personService = personService;
    }

}
