package edu.cornell.kfs.cemi.scm.remitto.batch.service.impl;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.kuali.kfs.core.api.config.Environment;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.vnd.VendorConstants;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.businessobject.VendorDetail;

import edu.cornell.kfs.cemi.scm.remitto.CemiRemiToSupplierConstants;
import edu.cornell.kfs.cemi.scm.remitto.batch.dto.CemiRemitToSupplierConnection;
import edu.cornell.kfs.cemi.scm.remitto.batch.service.CemiRemitToSupplierExtractService;
import edu.cornell.kfs.cemi.sys.CemiBaseConstants;
import edu.cornell.kfs.cemi.vnd.CemiVendorConstants;
import edu.cornell.kfs.cemi.vnd.CemiVendorParameterConstants;
import edu.cornell.kfs.cemi.vnd.batch.CreateCemiSupplierExtractStep;
import edu.cornell.kfs.cemi.vnd.dataaccess.CemiVendorDao;
import edu.cornell.kfs.cemi.vnd.dataaccess.CemiVendorOrmDao;

/**
 * Implementation of CemiRemitToSupplierExtractService.
 * 
 * Generates the Remit To Supplier Connection extract file by:
 * 1. Querying vendors with remit-to addresses
 * 2. Building connection DTOs
 * 3. Writing to Excel file based on template
 */
public class CemiRemitToSupplierExtractServiceImpl implements CemiRemitToSupplierExtractService {

    private static final Logger LOG = LogManager.getLogger();

    private final Environment environment;
    private String remitToSupplierFileCreationDirectory;
    private String remitToSupplierFileOutboundDirectory;
    private CemiVendorOrmDao cemiVendorOrmDao;
    private CemiVendorDao cemiVendorDao;
    private BusinessObjectService businessObjectService;
    private ParameterService parameterService;

    private DecimalFormat supplierIdFormatter;

    public CemiRemitToSupplierExtractServiceImpl(final Environment environment) {
        this.environment = environment;
        this.supplierIdFormatter = new DecimalFormat(CemiVendorConstants.SUPPLIER_ID_FORMAT);
    }

    @Override
    public void resetState() {
        LOG.debug("resetState, Service state has been reset");
    }

    @Override
    public void initializeExtractDateRangeSettings() {
    }

    @Override
    public void generateRemitToSupplierExtractFile(final LocalDateTime jobRunDate) {
        LOG.info("generateRemitToSupplierExtractFile, Starting Remit To Supplier extract generation");

        final boolean maskSensitiveData = shouldMaskCemiSensitiveData();
        final String outputFileName = buildOutputFileName(jobRunDate);
        final Path outputPath = Paths.get(remitToSupplierFileCreationDirectory, outputFileName);

        try (InputStream templateStream = getTemplateInputStream();
             Workbook workbook = new XSSFWorkbook(templateStream)) {

            final Sheet sheet = workbook.getSheet(CemiRemiToSupplierConstants.REMIT_TO_SUPPLIER_SHEET_NAME);
            if (sheet == null) {
                throw new IllegalStateException("Sheet '" + CemiRemiToSupplierConstants.REMIT_TO_SUPPLIER_SHEET_NAME + "' not found in template");
            }

            //final Iterator<VendorDetail> vendorIterator = cemiVendorOrmDao.findVendorsWithRemitAddresses();
            Stream<VendorDetail> vendors = cemiVendorOrmDao.getVendorsForCemiSupplierExtractAsCloseableStream();

            int rowIndex = CemiRemiToSupplierConstants.DATA_START_ROW;
            int vendorCount = 0;

            final Iterator<VendorDetail> vendorIterator  = vendors.iterator();
            while (vendorIterator.hasNext()) {
                final VendorDetail vendor = vendorIterator.next();
                final List<VendorAddress> remitAddresses = findRemitAddressesForVendor(vendor);

                if (remitAddresses.isEmpty()) {
                    LOG.debug("generateRemitToSupplierExtractFile, No remit addresses found for vendor {}-{}",
                            vendor.getVendorHeaderGeneratedIdentifier(), vendor.getVendorDetailAssignedIdentifier());
                    continue;
                }

                vendorCount++;
                final String supplierId = supplierIdFormatter.format(vendorCount);
                boolean isFirstConnection = true;

                for (final VendorAddress remitAddress : remitAddresses) {
                    final CemiRemitToSupplierConnection connection = buildConnectionFromVendorAddress(
                            vendor, remitAddress, supplierId, isFirstConnection);

                    writeConnectionToRow(sheet, rowIndex, connection);
                    rowIndex++;
                    isFirstConnection = false;
                }

                if (vendorCount % 100 == 0) {
                    LOG.info("generateRemitToSupplierExtractFile, Processed {} vendors...", vendorCount);
                }
            }

            // Write the workbook to file
            try (FileOutputStream fileOut = new FileOutputStream(outputPath.toFile())) {
                workbook.write(fileOut);
            }

            LOG.info("generateRemitToSupplierExtractFile, Completed. Processed {} vendors, wrote {} rows to {}",
                    vendorCount, rowIndex - CemiRemiToSupplierConstants.DATA_START_ROW, outputPath);

            // Copy to outbound directory
            copyToOutboundDirectory(outputPath, outputFileName);

        } catch (final IOException e) {
            LOG.error("generateRemitToSupplierExtractFile, Error generating extract file", e);
            throw new RuntimeException("Error generating Remit To Supplier extract file", e);
        }
    }

    private InputStream getTemplateInputStream() throws IOException {
        final InputStream templateStream = getClass().getClassLoader().getResourceAsStream(CemiRemiToSupplierConstants.REMIT_TO_SUPPLIER_TEMPLATE_FILE);
        if (templateStream == null) {
            throw new IOException("Template file not found: " + CemiRemiToSupplierConstants.REMIT_TO_SUPPLIER_TEMPLATE_FILE);
        }
        return templateStream;
    }

    private String buildOutputFileName(final LocalDateTime jobRunDate) {
        final String dateStamp = jobRunDate.format(CemiRemiToSupplierConstants.FILE_DATE_FORMAT);
        return CemiRemiToSupplierConstants.OUTPUT_FILE_NAME_FORMAT.replace("{0}", dateStamp);
    }
    
    private boolean isCemiSensitiveDataSetToUnmask() {
        String maskingParameterValue =  parameterService.getParameterValueAsString(
                CreateCemiSupplierExtractStep.class, CemiVendorParameterConstants.CEMI_SENSITIVE_DATA_MASKING_SETTING);
        return StringUtils.equalsIgnoreCase(maskingParameterValue, CemiBaseConstants.UNMASK);
    }

    private boolean isCemiEnvironment() {
        return StringUtils.equalsIgnoreCase(environment.getLane(), CemiBaseConstants.CEMI_ENVIRONMENT_LANE_NAME);
    }

    private boolean shouldUnmaskCemiSensitiveData() {
        return isCemiEnvironment() && isCemiSensitiveDataSetToUnmask();
    }

    private boolean shouldMaskCemiSensitiveData() {
        return !shouldUnmaskCemiSensitiveData();
    }

    private List<VendorAddress> findRemitAddressesForVendor(final VendorDetail vendor) {
        return vendor.getVendorAddresses().stream()
                .filter(VendorAddress::isActive)
                .filter(addr -> VendorConstants.AddressTypes.REMIT.equals(addr.getVendorAddressTypeCode()))
                .collect(Collectors.toList());
    }

    private CemiRemitToSupplierConnection buildConnectionFromVendorAddress(
            final VendorDetail vendor,
            final VendorAddress remitAddress,
            final String supplierId,
            final boolean isDefault) {

        final String connectionName = CemiRemitToSupplierConnection.buildConnectionName(
                vendor.getVendorName(), remitAddress.getVendorLine1Address());

        final String remitToAddressId = buildRemitToAddressId(supplierId, remitAddress);
        final String remitToEmail = StringUtils.defaultString(remitAddress.getVendorAddressEmailAddress());

        return new CemiRemitToSupplierConnection(
                supplierId,
                connectionName,
                CemiVendorConstants.DEFAULT_PAYMENT_TYPE,
                List.of(CemiVendorConstants.DEFAULT_PAYMENT_TYPE),
                KFSConstants.EMPTY_STRING, // settlementBankAccount - populated later if needed
                remitToAddressId,
                remitToEmail,
                KFSConstants.EMPTY_STRING, // payeeAlternateName
                KFSConstants.EMPTY_STRING, // alternateNameUsage
                isDefault,
                KFSConstants.EMPTY_STRING, // defaultPaymentTerms
                false); // alwaysSeparatePayments
    }

    private String buildRemitToAddressId(final String supplierId, final VendorAddress address) {
        return String.format("%s_ADDR_%d", supplierId, address.getVendorAddressGeneratedIdentifier());
    }

    private void writeConnectionToRow(final Sheet sheet, final int rowIndex, 
            final CemiRemitToSupplierConnection connection) {

        Row row = sheet.getRow(rowIndex);
        if (row == null) {
            row = sheet.createRow(rowIndex);
        }

        int colIndex = CemiRemiToSupplierConstants.START_COLUMN_INDEX;

        // Supplier
        setCellValue(row, colIndex++, connection.getSupplierId());
        // Supplier_Connection_Name
        setCellValue(row, colIndex++, connection.getSupplierConnectionName());
        // Default_Payment_Type
        setCellValue(row, colIndex++, connection.getDefaultPaymentType());
        // Accepted_Payment_Type_1, 2, 3
        for (final String paymentType : connection.getAcceptedPaymentTypes()) {
            setCellValue(row, colIndex++, paymentType);
        }
        // Settlement_Bank_Account
        setCellValue(row, colIndex++, connection.getSettlementBankAccount());
        // Remit_To_Address_ID
        setCellValue(row, colIndex++, connection.getRemitToAddressId());
        // Remit_To_Email_Address
        setCellValue(row, colIndex++, connection.getRemitToEmailAddress());
        // Payee_Alternate_Name
        setCellValue(row, colIndex++, connection.getPayeeAlternateName());
        // Alternate_Name_Usage
        setCellValue(row, colIndex++, connection.getAlternateNameUsage());
        // Is_Default
        setCellValue(row, colIndex++, connection.getIsDefault());
        // Default_Payment_Terms
        setCellValue(row, colIndex++, connection.getDefaultPaymentTerms());
        // Always_Separate_Payments
        setCellValue(row, colIndex++, connection.getAlwaysSeparatePayments());
    }

    private void setCellValue(final Row row, final int colIndex, final String value) {
        Cell cell = row.getCell(colIndex);
        if (cell == null) {
            cell = row.createCell(colIndex);
        }
        cell.setCellValue(StringUtils.defaultString(value));
    }

    private void copyToOutboundDirectory(final Path sourcePath, final String fileName) {
        try {
            final Path outboundPath = Paths.get(remitToSupplierFileOutboundDirectory, fileName);
            Files.copy(sourcePath, outboundPath);
            LOG.info("copyToOutboundDirectory, Copied file to outbound directory: {}", outboundPath);
        } catch (final IOException e) {
            LOG.error("copyToOutboundDirectory, Error copying file to outbound directory", e);
        }
    }

    public void setRemitToSupplierFileCreationDirectory(final String remitToSupplierFileCreationDirectory) {
        this.remitToSupplierFileCreationDirectory = remitToSupplierFileCreationDirectory;
    }

    public void setRemitToSupplierFileOutboundDirectory(final String remitToSupplierFileOutboundDirectory) {
        this.remitToSupplierFileOutboundDirectory = remitToSupplierFileOutboundDirectory;
    }

    public void setCemiVendorOrmDao(final CemiVendorOrmDao cemiVendorOrmDao) {
        this.cemiVendorOrmDao = cemiVendorOrmDao;
    }

    public void setCemiVendorDao(final CemiVendorDao cemiVendorDao) {
        this.cemiVendorDao = cemiVendorDao;
    }

    public void setBusinessObjectService(final BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public void setParameterService(final ParameterService parameterService) {
        this.parameterService = parameterService;
    }

}