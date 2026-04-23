package edu.cornell.kfs.cemi.vnd.batch.service.impl;

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

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.springframework.core.env.Environment;

import edu.cornell.kfs.cemi.sys.CemiBaseConstants;
import edu.cornell.kfs.cemi.vnd.CemiVendorConstants;
import edu.cornell.kfs.cemi.vnd.batch.CreateCemiSupplierExtractStep;
import edu.cornell.kfs.cemi.vnd.batch.dto.CemiRemitToSupplierConnection;
import edu.cornell.kfs.cemi.vnd.batch.service.CemiRemitToSupplierExtractService;
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

    private static final String REMIT_TO_SUPPLIER_TEMPLATE_FILE = "edu/cornell/kfs/cemi/vnd/batch/remitToSupplier/Remit_To_Supplier.xlsx";
    private static final String REMIT_TO_SUPPLIER_SHEET_NAME = "Remit_To_Supplier";
    private static final int DATA_START_ROW = 10; // Row 11 in Excel (0-based)
    private static final int START_COLUMN_INDEX = 0;

    private static final String OUTPUT_FILE_NAME_FORMAT = "Remit_To_Supplier_{0}.xlsx";
    private static final DateTimeFormatter FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final Environment environment;
    private String remitToSupplierFileCreationDirectory;
    private String remitToSupplierFileOutboundDirectory;
    private CemiVendorOrmDao cemiVendorOrmDao;
    private CemiVendorDao cemiVendorDao;
    private BusinessObjectService businessObjectService;
    private ParameterService parameterService;

    private LocalDateTime vendorActivityFromDate;
    private LocalDateTime vendorActivityToDate;
    private DecimalFormat supplierIdFormatter;

    public CemiRemitToSupplierExtractServiceImpl(final Environment environment) {
        this.environment = environment;
        this.supplierIdFormatter = new DecimalFormat(CemiVendorConstants.SUPPLIER_ID_FORMAT);
    }

    @Override
    public void resetState() {
        this.vendorActivityFromDate = null;
        this.vendorActivityToDate = null;
        LOG.debug("resetState, Service state has been reset");
    }

    @Override
    public void initializeExtractDateRangeSettings() {
        final String fromDateString = parameterService.getParameterValueAsString(
                CreateCemiSupplierExtractStep.class, CemiVendorConstants.Parameters.VENDOR_ACTIVITY_FROM_DATE);
        final String toDateString = parameterService.getParameterValueAsString(
                CreateCemiSupplierExtractStep.class, CemiVendorConstants.Parameters.VENDOR_ACTIVITY_TO_DATE);

        this.vendorActivityFromDate = CemiVendorConstants.parseVendorActivityDate(fromDateString);
        this.vendorActivityToDate = CemiVendorConstants.parseVendorActivityDate(toDateString);

        LOG.info("initializeExtractDateRangeSettings, Using date range: {} to {}", 
                vendorActivityFromDate, vendorActivityToDate);
    }

    @Override
    public void generateRemitToSupplierExtractFile(final LocalDateTime jobRunDate) {
        LOG.info("generateRemitToSupplierExtractFile, Starting Remit To Supplier extract generation");

        final boolean maskSensitiveData = determineMaskSensitiveDataSetting();
        final String outputFileName = buildOutputFileName(jobRunDate);
        final Path outputPath = Paths.get(remitToSupplierFileCreationDirectory, outputFileName);

        try (InputStream templateStream = getTemplateInputStream();
             Workbook workbook = new XSSFWorkbook(templateStream)) {

            final Sheet sheet = workbook.getSheet(REMIT_TO_SUPPLIER_SHEET_NAME);
            if (sheet == null) {
                throw new IllegalStateException("Sheet '" + REMIT_TO_SUPPLIER_SHEET_NAME + "' not found in template");
            }

            final Iterator<VendorDetail> vendorIterator = cemiVendorOrmDao.findVendorsWithRemitAddresses(
                    vendorActivityFromDate, vendorActivityToDate);

            int rowIndex = DATA_START_ROW;
            int vendorCount = 0;

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
                    vendorCount, rowIndex - DATA_START_ROW, outputPath);

            // Copy to outbound directory
            copyToOutboundDirectory(outputPath, outputFileName);

        } catch (final IOException e) {
            LOG.error("generateRemitToSupplierExtractFile, Error generating extract file", e);
            throw new RuntimeException("Error generating Remit To Supplier extract file", e);
        }
    }

    private InputStream getTemplateInputStream() throws IOException {
        final InputStream templateStream = getClass().getClassLoader().getResourceAsStream(REMIT_TO_SUPPLIER_TEMPLATE_FILE);
        if (templateStream == null) {
            throw new IOException("Template file not found: " + REMIT_TO_SUPPLIER_TEMPLATE_FILE);
        }
        return templateStream;
    }

    private String buildOutputFileName(final LocalDateTime jobRunDate) {
        final String dateStamp = jobRunDate.format(FILE_DATE_FORMAT);
        return OUTPUT_FILE_NAME_FORMAT.replace("{0}", dateStamp);
    }

    private boolean determineMaskSensitiveDataSetting() {
        final String currentEnvironment = environment.getProperty(CemiBaseConstants.CEMI_ENVIRONMENT_PROPERTY);
        return !StringUtils.equalsIgnoreCase(CemiBaseConstants.CEMI_ENVIRONMENT_VALUE, currentEnvironment);
    }

    private List<VendorAddress> findRemitAddressesForVendor(final VendorDetail vendor) {
        return vendor.getVendorAddresses().stream()
                .filter(VendorAddress::isActive)
                .filter(addr -> CemiVendorConstants.AddressTypeCodes.REMIT.equals(addr.getVendorAddressTypeCode()))
                .toList();
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

        int colIndex = START_COLUMN_INDEX;

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