package  edu.cornell.kfs.cemi.module.purap.batch.service.impl;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.config.Environment;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.cemi.module.purap.CemiPurchaseOrderConstants;
import edu.cornell.kfs.cemi.module.purap.CemiPurchaseOrderParameterConstants;
import edu.cornell.kfs.cemi.module.purap.batch.CreateCemiPurchaseOrderExtractStep;
import edu.cornell.kfs.cemi.module.purap.batch.businessobject.CemiPurchaseOrderIdBo;
import edu.cornell.kfs.cemi.module.purap.batch.service.CemiPurchaseOrderExtractService;
import edu.cornell.kfs.cemi.module.purap.batch.service.CemiPurchaseOrderFileExtractDataBuilder;
import edu.cornell.kfs.cemi.module.purap.dataaccess.CemiPurchaseOrderExtractDao;
import edu.cornell.kfs.cemi.module.purap.dataaccess.CemiPurchaseOrderExtractOrmDao;
import edu.cornell.kfs.cemi.sys.batch.service.impl.CemiDataExtractServiceBase;
import edu.cornell.kfs.cemi.sys.util.CemiUtils;

public class CemiPurchaseOrderExtractServiceImpl extends CemiDataExtractServiceBase
        implements CemiPurchaseOrderExtractService {

    private static final Logger LOG = LogManager.getLogger();

    private CemiPurchaseOrderExtractOrmDao cemiPurchaseOrderExtractOrmDao;
    private CemiPurchaseOrderExtractDao cemiPurchaseOrderExtractDao;
    private BusinessObjectService businessObjectService;
    private DocumentService documentService;
    private DataDictionaryService dataDictionaryService;
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

        try (
                final Stream<CemiPurchaseOrderIdBo> purchaseOrderDocIds =
                        cemiPurchaseOrderExtractOrmDao.getIdsOfPurchaseOrdersToExtractAsCloseableStream();
        ) {
            final String jobRunDateString = CemiUtils.generateBatchJobRunDateAsString(jobRunDate);
            final String supplierJobRunDateString = getSupplierJobRunDateString();
            final CemiPurchaseOrderFileExtractDataBuilder dataBuilder = new CemiPurchaseOrderFileExtractDataBuilderDefaultImpl(
                    businessObjectService, jobRunDateString, personService, parameterService,
                    cemiPurchaseOrderExtractDao, supplierJobRunDateString, shouldMaskCemiSensitiveData());
            final Iterator<CemiPurchaseOrderIdBo> purchaseOrderDocIdsIterator = purchaseOrderDocIds.iterator();
            final Iterator<PurchaseOrderDocument> purchaseOrdersIterator = new CemiPurchaseOrderIterator(
                    purchaseOrderDocIdsIterator, documentService, dataDictionaryService);
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

    public void setCemiPurchaseOrderExtractOrmDao(final CemiPurchaseOrderExtractOrmDao cemiPurchaseOrderExtractOrmDao) {
        this.cemiPurchaseOrderExtractOrmDao = cemiPurchaseOrderExtractOrmDao;
    }

    public void setCemiPurchaseOrderExtractDao(final CemiPurchaseOrderExtractDao cemiPurchaseOrderExtractDao) {
        this.cemiPurchaseOrderExtractDao = cemiPurchaseOrderExtractDao;
    }

    public void setBusinessObjectService(final BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public void setDocumentService(final DocumentService documentService) {
        this.documentService = documentService;
    }

    public void setDataDictionaryService(final DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
    }

    public void setPersonService(final PersonService personService) {
        this.personService = personService;
    }

}
