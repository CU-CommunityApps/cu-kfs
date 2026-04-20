package edu.cornell.kfs.cemi.vnd.batch.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.stream.IntStream;

import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSheetDimension;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorksheet;

import edu.cornell.kfs.cemi.sys.batch.CemiOutputDefinitionFileType;
import edu.cornell.kfs.cemi.sys.batch.service.impl.CemiExcelWriter;
import edu.cornell.kfs.cemi.sys.batch.xml.CemiOutputDefinition;
import edu.cornell.kfs.cemi.sys.batch.xml.CemiSheetDefinition;
import edu.cornell.kfs.cemi.sys.util.CemiUtils;
import edu.cornell.kfs.cemi.vnd.CemiVendorConstants;
import edu.cornell.kfs.core.api.util.CuCoreUtilities;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.util.CreateTestDirectories;
import edu.cornell.kfs.sys.util.GlobalResourceLoaderUtils;
import edu.cornell.kfs.sys.util.TestSpringContextExtension;
import edu.cornell.kfs.cemi.vnd.CemiVendorTestConstants.VendorSpringBeans;

@CreateTestDirectories(
    baseDirectory = WriteCemiSupplierFileTest.CEMI_SUPPLIER_DIRECTORY,
    subDirectories = {
        WriteCemiSupplierFileTest.REPORTS_DIRECTORY,
        WriteCemiSupplierFileTest.STAGING_SYS_DIRECTORY,
        WriteCemiSupplierFileTest.STAGING_VND_DIRECTORY
    }
)
@Execution(ExecutionMode.SAME_THREAD)
public class WriteCemiSupplierFileTest {

    static final String BASE_TEST_DIRECTORY = "test/";
    static final String CEMI_SUPPLIER_DIRECTORY = BASE_TEST_DIRECTORY + "cemiSupplierExtractTest/";
    static final String REPORTS_DIRECTORY = CEMI_SUPPLIER_DIRECTORY + "reports/";
    static final String STAGING_SYS_DIRECTORY = CEMI_SUPPLIER_DIRECTORY + "staging/sys/";
    static final String STAGING_VND_DIRECTORY = CEMI_SUPPLIER_DIRECTORY + "staging/vnd/";

    private static final DateTimeFormatter FILE_DATE_TIME_FORMATTER = DateTimeFormatter
            .ofPattern(CUKFSConstants.DATE_FORMAT_yyyyMMdd_HHmmss, Locale.US)
            .withZone(ZoneId.of(CUKFSConstants.TIME_ZONE_US_EASTERN));

    @RegisterExtension
    static TestSpringContextExtension springContextExtension = TestSpringContextExtension.forClassPathSpringXmlFile(
            "edu/cornell/kfs/vnd/batch/service/impl/cu-spring-vnd-cemi-supplier-file-test.xml");

    private CemiOutputDefinitionFileType outputDefinitionFileType;
    private CemiOutputDefinition outputDefinition;

    @BeforeEach
    void setUp() throws Exception {
        outputDefinitionFileType = springContextExtension.getBean(VendorSpringBeans.CEMI_OUTPUT_DEFINITION_FILE_TYPE,
                CemiOutputDefinitionFileType.class);

        outputDefinition = GlobalResourceLoaderUtils.doWithResourceRetrievalDelegatedToKradResourceLoaderUtil(() -> {
            try (
                final InputStream definitionStream = CuCoreUtilities.getResourceAsStream(
                        CemiVendorConstants.SUPPLIER_OUTPUT_DEFINITION_FILE_PATH);
            ) {
                final byte[] fileContents = IOUtils.toByteArray(definitionStream);
                return outputDefinitionFileType.parse(fileContents);
            }
        });
    }

    @AfterEach
    void tearDown() throws Exception {
        outputDefinition = null;
        outputDefinitionFileType = null;
    }

    @Test    
    void testGenerateXlsxWithMinimalDataRows() throws Exception {
        final LocalDateTime localDateTime = LocalDateTime.now();
        final String stringDateTime = FILE_DATE_TIME_FORMATTER.format(localDateTime);
        final String fileName = "Supplier_" + stringDateTime + ".xlsx";
        /*
         * NOTE: If you want the generated file to persist after the test completes,
         *       update the variable below to use the BASE_TEST_DIRECTORY constant instead.
         *       Doing so will make the test place the file directly under "cu-kfs/test".
         */
        final String path = STAGING_VND_DIRECTORY;
        final File file = new File(path + fileName);

        createAndPopulateExcelFileFromTemplate(file);
        assertFileWasGeneratedWithoutUsingZip64(file);
        assertGeneratedFileHasExpectedStructureInModifiedSheets(file);
    }

    private void createAndPopulateExcelFileFromTemplate(final File file) throws Exception {
        try (
            final InputStream templateStream = CuCoreUtilities.getResourceAsStream(
                    CemiVendorConstants.SUPPLIER_TEMPLATE_FILE_PATH);
            final CemiExcelWriter excelWriter = new CemiExcelWriter(outputDefinition, templateStream, file);
        ) {
            for (final CemiSheetDefinition sheet : outputDefinition.getSheets()) {
                final String[] testDataRow = createRowFilledWithSimpleTestData(sheet);
                excelWriter.writeRow(sheet.getName(), testDataRow);
            }

            excelWriter.commit();
        }
    }

    private String[] createRowFilledWithSimpleTestData(final CemiSheetDefinition sheet) {
        final int fieldCount = sheet.getFields().size();
        return IntStream.range(0, fieldCount)
                .mapToObj(index -> "V" + index)
                .toArray(String[]::new);
    }

    private void assertFileWasGeneratedWithoutUsingZip64(final File file) throws Exception {
        try (
            final ZipFile zipFile = ZipFile.builder()
                    .setFile(file)
                    .get();
        ) {
            final boolean zip64IsPotentiallyEnabled = zipFile.stream()
                    .anyMatch(entry -> entry.getCentralDirectoryExtra().length > 0);
            assertFalse(zip64IsPotentiallyEnabled,
                    "Extra data was detected in the workbook zip's central directory; Zip64 might be enabled!");
        }
    }

    private void assertGeneratedFileHasExpectedStructureInModifiedSheets(final File file) throws Exception {
        try (
            final InputStream oldFileStream = CuCoreUtilities.getResourceAsStream(
                    CemiVendorConstants.SUPPLIER_TEMPLATE_FILE_PATH);
            final InputStream newFileStream = new FileInputStream(file);
            final OPCPackage oldOpcPackage = OPCPackage.open(oldFileStream);
            final OPCPackage newOpcPackage = OPCPackage.open(newFileStream);
            final XSSFWorkbook oldWorkbook = new XSSFWorkbook(oldOpcPackage);
            final XSSFWorkbook newWorkbook = new XSSFWorkbook(newOpcPackage);
        ) {
            for (final CemiSheetDefinition sheetDefinition : outputDefinition.getSheets()) {
                final String sheetName = sheetDefinition.getName();
                final XSSFSheet oldSheet = oldWorkbook.getSheet(sheetName);
                final XSSFSheet newSheet = newWorkbook.getSheet(sheetName);
                assertNotNull(oldSheet, "Sheet not found in original workbook: " + sheetName);
                assertNotNull(newSheet, "Sheet not found in generated workbook: " + sheetName);
                assertSheetHasCorrectDimensions(sheetDefinition, newSheet);
                assertSheetHasCorrectHeaderRows(sheetDefinition, oldSheet, newSheet);
                assertSheetHasCorrectTestDataRow(sheetDefinition, newSheet);
            }
        }
    }

    private void assertSheetHasCorrectDimensions(final CemiSheetDefinition sheetDefinition, final XSSFSheet sheet) {
        final String sheetName = sheetDefinition.getName();
        final String expectedUpperLeftBounds = "A1";
        final String expectedLowerRightBounds = getExpectedLowerRightRefBounds(sheetDefinition);
        final CTWorksheet lowLevelSheet = sheet.getCTWorksheet();
        final CTSheetDimension sheetDimension = lowLevelSheet.getDimension();
        final String ref = sheetDimension.getRef();
        assertTrue(Strings.CS.contains(ref, CUKFSConstants.COLON),
                "Missing colon in dimension ref for sheet: " + sheetName);
        final String refStart = StringUtils.substringBefore(ref, CUKFSConstants.COLON);
        final String refEnd = StringUtils.substringAfter(ref, CUKFSConstants.COLON);
        assertEquals(expectedUpperLeftBounds, refStart, "Wrong top-left ref boundary for sheet: " + sheetName);
        assertEquals(expectedLowerRightBounds, refEnd, "Wrong lower-right ref boundary for sheet: " + sheetName);
    }

    private String getExpectedLowerRightRefBounds(final CemiSheetDefinition sheetDefinition) {
        final int numInsertedDataRows = 1;
        final int expectedRowCount = CemiUtils.getHeaderRowCount(sheetDefinition) + numInsertedDataRows;
        final int expectedColumnCount = CemiUtils.getFullColumnCount(sheetDefinition);
        final String expectedColumnString = CellReference.convertNumToColString(expectedColumnCount - 1);
        return expectedColumnString + Integer.toString(expectedRowCount);
    }

    private void assertSheetHasCorrectHeaderRows(
            final CemiSheetDefinition sheetDefinition, final XSSFSheet oldSheet, final XSSFSheet newSheet) {
        final String sheetName = sheetDefinition.getName();
        final int headerRowCount = CemiUtils.getHeaderRowCount(sheetDefinition);
        final int columnCount = CemiUtils.getFullColumnCount(sheetDefinition);
        for (int rowIndex = 0; rowIndex < headerRowCount; rowIndex++) {
            final XSSFRow oldRow = oldSheet.getRow(rowIndex);
            final XSSFRow newRow = newSheet.getRow(rowIndex);
            assertNotNull(oldRow, "Row index " + rowIndex + " not found in original sheet: " + sheetName);
            assertNotNull(newRow, "Row index " + rowIndex + " not found in generated sheet: " + sheetName);

            assertEquals(oldRow.getHeightInPoints(), newRow.getHeightInPoints(),
                    "Wrong height for row at index " + rowIndex + " in sheet " + sheetName);
            assertEquals(oldRow.getZeroHeight(), newRow.getZeroHeight(),
                    "Wrong zero-height setting for row at index " + rowIndex + " in sheet " + sheetName);
            assertEquals(oldRow.isFormatted(), newRow.isFormatted(),
                    "Wrong presence of row-level formatting for row at index " + rowIndex + " in sheet " + sheetName);
            assertRowHasCorrectHeaderCells(sheetName, oldRow, newRow, columnCount);
        }
    }

    private void assertRowHasCorrectHeaderCells(
            final String sheetName, final XSSFRow oldRow, final XSSFRow newRow, final int columnCount) {
        final int rowIndex = oldRow.getRowNum();
        final String rowAndSheetMessage = " for row at index " + rowIndex + " in sheet " + sheetName;
        for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
            final XSSFCell oldCell = oldRow.getCell(columnIndex);
            final XSSFCell newCell = newRow.getCell(columnIndex);

            if (oldCell == null) {
                assertNull(newCell,
                        "Cell at index " + columnIndex + " should not have been present" + rowAndSheetMessage);
            } else {
                assertNotNull(newCell,
                        "Cell at index " + columnIndex + " should not have been present" + rowAndSheetMessage);
                assertEquals(oldCell.getCellType(), newCell.getCellType(),
                        "Wrong cell type at index " + columnIndex + rowAndSheetMessage);

                switch (oldCell.getCellType()) {
                    case STRING :
                        assertEquals(oldCell.getStringCellValue(), newCell.getStringCellValue(),
                                "Wrong string value at cell index " + columnIndex + rowAndSheetMessage);
                        break;

                    case NUMERIC :
                        assertEquals(oldCell.getNumericCellValue(), newCell.getNumericCellValue(),
                                "Wrong numeric value at cell index " + columnIndex + rowAndSheetMessage);
                        break;

                    case BOOLEAN :
                        assertEquals(oldCell.getBooleanCellValue(), newCell.getBooleanCellValue(),
                                "Wrong boolean value at cell index " + columnIndex + rowAndSheetMessage);
                        break;

                    default :
                        break;
                }
            }
        }
    }

    private void assertSheetHasCorrectTestDataRow(final CemiSheetDefinition sheetDefinition, final XSSFSheet newSheet) {
        final String sheetName = newSheet.getSheetName();
        final int rowIndex = CemiUtils.getHeaderRowCount(sheetDefinition);
        final String rowAndSheetMessage = " for row at index " + rowIndex + " in sheet " + sheetName;
        final int startColumnIndex = sheetDefinition.getStartColumnIndex();
        final int columnCount = CemiUtils.getDataColumnCount(sheetDefinition);
        final XSSFRow dataRow = newSheet.getRow(rowIndex);
        assertNotNull(dataRow, "Data row at index " + rowIndex + " not found in sheet " + sheetName);

        for (int index = 0; index < columnCount; index++) {
            final int columnIndex = index + startColumnIndex;
            final String expectedValue = "V" + index;
            final XSSFCell cell = dataRow.getCell(columnIndex);
            assertNotNull(cell, "Cell not found at index " + columnIndex + rowAndSheetMessage);
            assertEquals(CellType.STRING, cell.getCellType(),
                    "Wrong cell type at index " + columnIndex + rowAndSheetMessage);
            assertEquals(expectedValue, cell.getStringCellValue(),
                    "Wrong value at cell index " + columnIndex + rowAndSheetMessage);
        }
    }

}
