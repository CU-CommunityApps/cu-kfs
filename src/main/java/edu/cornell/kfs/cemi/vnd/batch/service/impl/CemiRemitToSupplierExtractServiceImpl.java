package edu.cornell.kfs.cemi.vnd.batch.service.impl;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.config.Environment;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.cemi.sys.batch.service.impl.CemiDataExtractServiceBase;
import edu.cornell.kfs.cemi.sys.util.CemiUtils;
import edu.cornell.kfs.cemi.vnd.CemiRemitToSupplierConstants;
import edu.cornell.kfs.cemi.vnd.CemiRemitToSupplierParameterConstants;
import edu.cornell.kfs.cemi.vnd.batch.CreateCemiRemitToSupplierExtractStep;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierFileAddressesTabRowBo;
import edu.cornell.kfs.cemi.vnd.batch.service.CemiRemitToSupplierExtractService;
import edu.cornell.kfs.cemi.vnd.dataaccess.CemiRemitToSupplierExtractDao;
import edu.cornell.kfs.cemi.vnd.dataaccess.CemiRemitToSupplierExtractOrmDao;

public class CemiRemitToSupplierExtractServiceImpl extends CemiDataExtractServiceBase
        implements CemiRemitToSupplierExtractService {

    private static final Logger LOG = LogManager.getLogger();

    private CemiRemitToSupplierExtractOrmDao cemiRemitToSupplierExtractOrmDao;
    private CemiRemitToSupplierExtractDao cemiRemitToSupplierExtractDao;
    private BusinessObjectService businessObjectService;

    public CemiRemitToSupplierExtractServiceImpl(final Environment environment) {
        super(environment);
    }

    @Override
    public void logSystemParameterValues() {
        LOG.info("logSystemParameterValues, Environment lane is '{}', sensitive data will be {}",
                environment.getLane(), shouldMaskCemiSensitiveData() ? "masked" : "unmasked");
        CemiUtils.logParameterValues(parameterService, CreateCemiRemitToSupplierExtractStep.class, List.of(
                CemiRemitToSupplierParameterConstants.COPY_CEMI_REMIT_TO_SUPPLIER_FILE_TO_OUTBOUND_FOLDER,
                CemiSupplierParameterConstants.CEMI_SENSITIVE_DATA_MASKING_SETTING,
                CemiRemitToSupplierParameterConstants.CEMI_REMIT_TO_SUPPLIER_EXTRACT_SUPPLIER_DATETIME));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void resetState() {
        LOG.info("resetState, Deleting the list of extractable addresses from the previous run (if present)...");
        cemiRemitToSupplierExtractDao.clearExistingListOfExtractableRemitAddressIds();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void captureInScopeBusinessObjectKeysToProcessingTable() {
        initializeExtractDateSettings();
        LOG.info("captureInScopeBusinessObjectKeysToProcessingTable, Querying and storing the list of "
                + "extractable addresses...");
        cemiRemitToSupplierExtractDao.queryAndStoreAddressIdsForRemitToSupplierExtract();
    }

    private void initializeExtractDateSettings() {
        LOG.info("initializeExtractDateSettings, Setting Supplier extraction date-time to use for address queries...");
        final String supplierJobRunDateString = getSupplierJobRunDateString();
        cemiRemitToSupplierExtractDao.updateRemitToSupplierExtractQuerySettings(supplierJobRunDateString);
    }

    private String getSupplierJobRunDateString() {
        final String supplierJobRunDateString = parameterService.getParameterValueAsString(
                CreateCemiRemitToSupplierExtractStep.class,
                CemiRemitToSupplierParameterConstants.CEMI_REMIT_TO_SUPPLIER_EXTRACT_SUPPLIER_DATETIME);
        Validate.validState(StringUtils.isNotBlank(supplierJobRunDateString), "Parameter %s should not have been blank",
                CemiRemitToSupplierParameterConstants.CEMI_REMIT_TO_SUPPLIER_EXTRACT_SUPPLIER_DATETIME);
        return supplierJobRunDateString;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void generateIntermediateExtractData(final LocalDateTime jobRunDate) {
        LOG.info("generateIntermediateExtractData, Generating data rows for Remit To Supplier spreadsheet "
                + "and placing in intermediate storage...");
        try {
            generateRemitToSupplierExtractData(jobRunDate);
        } catch (final Exception e) {
            LOG.error("generateIntermediateExtractData, Creation of Remit To Supplier Extract data failed", e);
            throw new RuntimeException(e);
        }
    }

    private void generateRemitToSupplierExtractData(final LocalDateTime jobRunDate) throws IOException {
        try (
            final Stream<CemiSupplierFileAddressesTabRowBo> addresses = cemiRemitToSupplierExtractOrmDao
                    .getAddressesForCemiRemitToSupplierExtractAsCloseableStream();
        ) {
            final String jobRunDateString = CemiUtils.generateBatchJobRunDateAsString(jobRunDate);
            final String supplierJobRunDate = getSupplierJobRunDateString();
            final CemiRemitToSupplierFileExtractDataBuilderDefaultImpl dataBuilder
                    = new CemiRemitToSupplierFileExtractDataBuilderDefaultImpl(
                            businessObjectService, jobRunDateString, supplierJobRunDate, shouldMaskCemiSensitiveData());
            final Iterator<CemiSupplierFileAddressesTabRowBo> addressesIterator = addresses.iterator();
            dataBuilder.writeRemitToSupplierFileRemitToSupplierTabExtractDataToIntermediateStorage(addressesIterator);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void generateDataConversionExtractFile(final LocalDateTime jobRunDate) {
        LOG.info("generateEntityContactExtractFile, Starting creation of CEMI Extract file {}",
                CemiRemitToSupplierConstants.REMIT_TO_SUPPLIER_EXTRACT_PLAIN_FILENAME);
        generateFileForDataExtract(jobRunDate, CemiRemitToSupplierConstants.REMIT_TO_SUPPLIER_EXTRACT_PLAIN_FILENAME,
                CemiRemitToSupplierConstants.REMIT_TO_SUPPLIER_EXTRACT_FILENAME_PREFIX);
    }

    @Override
    protected Class<?> getComponentClassForDataExtractParameter() {
        return CreateCemiRemitToSupplierExtractStep.class;
    }

    @Override
    protected String getOutputDefinitionFilePathSuffix() {
        return CemiRemitToSupplierConstants.REMIT_TO_SUPPLIER_OUTPUT_DEFINITION_PATH_SUFFIX;
    }

    @Override
    protected String getTemplateWorkbookFilePathSuffix() {
        return CemiRemitToSupplierConstants.REMIT_TO_SUPPLIER_TEMPLATE_WORKBOOK_FILE_PATH_SUFFIX;
    }

    public void setBusinessObjectService(final BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public void setCemiRemitToSupplierExtractOrmDao(
            final CemiRemitToSupplierExtractOrmDao cemiRemitToSupplierExtractOrmDao) {
        this.cemiRemitToSupplierExtractOrmDao = cemiRemitToSupplierExtractOrmDao;
    }

    public void setCemiRemitToSupplierExtractDao(
            final CemiRemitToSupplierExtractDao cemiRemitToSupplierExtractDao) {
        this.cemiRemitToSupplierExtractDao = cemiRemitToSupplierExtractDao;
    }

}
