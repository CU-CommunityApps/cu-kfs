package edu.cornell.kfs.vnd.batch.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import edu.cornell.kfs.sys.util.CreateTestDirectories;

/**
 * Simple proof-of-concept test that does the following:
 * 
 * 1. Copies an existing .xlsx template file into the test folder structure.
 * 2. Reads the copied .xlsx template file.
 * 3. Removes any non-header rows from the file's "Supplier" sheet (such as example data rows).
 * 4. Writes a simple data row to the "Supplier" sheet, with the first column intentionally left blank.
 * 5. Overwrites the copied .xlsx template file with the updated contents.
 * 
 * NOTE: In order for the file overwrite to work as expected, the in-memory workbook needs to be
 * initialized by using an input stream, NOT by using a File object directly!
 */
@Execution(ExecutionMode.SAME_THREAD)
@CreateTestDirectories(
        baseDirectory = AppendSpreadsheetTest.TEST_VND_DIRECTORY,
        subDirectories = {
                AppendSpreadsheetTest.TEST_VND_STAGING_DIRECTORY
        }
)
public class AppendSpreadsheetTest {

    public static final String TEST_VND_DIRECTORY = "test/vnd_append_spreadsheet/";
    public static final String TEST_VND_STAGING_DIRECTORY = TEST_VND_DIRECTORY + "staging/vnd/";

    public static final String TEMPLATE_SRC_PATH = "C:/Users/cah292/Downloads/Supplier.xlsx";
    public static final String TEMPLATE_DEST_FILE = "supplier_template.xlsx";
    public static final String SUPPLIER_SHEET_NAME = "Supplier";
    public static final int TEMPLATE_SUPPLIER_ROW_COUNT_TO_KEEP = 6;
    public static final int SUPPLIER_COLUMN_COUNT = 52;

    @Test
    void testAppendRowToExistingSpreadsheet() throws Exception {
        copyTemplateToTestDirectory();
        appendSupplierRowToSpreadsheet();
        System.out.println("SUCCESS! Putting a breakpoint here will allow you "
                + "to manually access/copy the file before this test class auto-deletes it.");
    }

    private void copyTemplateToTestDirectory() throws IOException {
        final File originalTemplateFile = new File(TEMPLATE_SRC_PATH);
        final File copiedTemplateFile = getCopiedTemplateFile();
        FileUtils.copyFile(originalTemplateFile, copiedTemplateFile);
    }

    private void appendSupplierRowToSpreadsheet() throws Exception {
        final File spreadsheetFile = getCopiedTemplateFile();
        try (
            final FileInputStream inputStream = new FileInputStream(spreadsheetFile);
            final OPCPackage opcPackage = OPCPackage.open(inputStream);
            final XSSFWorkbook templateWorkbook = new XSSFWorkbook(opcPackage);
        ) {
            appendSupplierRowToSpreadsheet(templateWorkbook);
        }
    }

    private void appendSupplierRowToSpreadsheet(final XSSFWorkbook templateWorkbook) throws Exception {
        removeUnwantedTemplateRows(templateWorkbook, SUPPLIER_SHEET_NAME, TEMPLATE_SUPPLIER_ROW_COUNT_TO_KEEP);
        try (
            final SXSSFWorkbook workbook = new SXSSFWorkbook(templateWorkbook);
            final FileOutputStream outStream = new FileOutputStream(getCopiedTemplateFile());
        ) {
            final SXSSFSheet supplierSheet = workbook.getSheet(SUPPLIER_SHEET_NAME);
            final int rowNum = TEMPLATE_SUPPLIER_ROW_COUNT_TO_KEEP;
            final SXSSFRow newRow = supplierSheet.createRow(rowNum);
            for (int i = 1; i < SUPPLIER_COLUMN_COUNT; i++) {
                SXSSFCell cell = newRow.createCell(i, CellType.STRING);
                cell.setCellValue("DATA" + i);
            }
            supplierSheet.flushBufferedData();
            workbook.write(outStream);
        }
    }

    private File getCopiedTemplateFile() {
        return new File(TEST_VND_STAGING_DIRECTORY + TEMPLATE_DEST_FILE).getAbsoluteFile();
    }

    private void removeUnwantedTemplateRows(
            final XSSFWorkbook workbook, final String sheetName, final int numRowsToKeep) {
        final XSSFSheet sheet = workbook.getSheet(sheetName);
        int lastRowNum = sheet.getLastRowNum();
        System.out.println("Existing last row num: " + lastRowNum);
        while (lastRowNum >= numRowsToKeep) {
            final XSSFRow rowToDelete = sheet.getRow(lastRowNum);
            sheet.removeRow(rowToDelete);
            lastRowNum = sheet.getLastRowNum();
        }
        System.out.println("Final last row num: " + lastRowNum);
    }

}
