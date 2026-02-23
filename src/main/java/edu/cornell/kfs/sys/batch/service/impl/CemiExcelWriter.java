package edu.cornell.kfs.sys.batch.service.impl;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import edu.cornell.kfs.sys.batch.xml.CemiOutputDefinition;
import edu.cornell.kfs.sys.batch.xml.CemiSheetDefinition;

public class CemiExcelWriter implements Closeable {

    private static final Logger LOG = LogManager.getLogger();

    private final Map<String, CemiSheet> sheets;

    private FileInputStream fileInputStream;
    private OPCPackage opcPackage;
    private XSSFWorkbook templateWorkbook;
    private SXSSFWorkbook streamedWorkbook;
    private FileOutputStream fileOutputStream;
    private boolean committed;

    public CemiExcelWriter(final CemiOutputDefinition outputDefinition, final File templateFile)
            throws IOException, InvalidFormatException {
        Validate.notNull(outputDefinition, "outputDefinition cannot be null");
        Validate.notNull(templateFile, "templateFile cannot be null");
        boolean setupSucceeded = false;

        try {
            fileInputStream = new FileInputStream(templateFile);
            opcPackage = OPCPackage.open(fileInputStream);
            templateWorkbook = new XSSFWorkbook(opcPackage);
            removeNonHeaderRowsFromTemplate(outputDefinition, templateWorkbook);
            streamedWorkbook = new SXSSFWorkbook(templateWorkbook);
            fileOutputStream = new FileOutputStream(templateFile);

            this.sheets = createSheetsMap(outputDefinition, streamedWorkbook);
            committed = false;
            setupSucceeded = true;
        } finally {
            if (!setupSucceeded) {
                IOUtils.closeQuietly(fileOutputStream, streamedWorkbook, templateWorkbook, opcPackage, fileInputStream);
            }
        }
    }

    private static void removeNonHeaderRowsFromTemplate(final CemiOutputDefinition outputDefinition,
            final XSSFWorkbook templateWorkbook) {
        for (final CemiSheetDefinition sheetDefinition : outputDefinition.getSheets()) {
            final XSSFSheet sheet = templateWorkbook.getSheet(sheetDefinition.getName());
            Validate.validState(sheet != null, "Sheet not found in template: %s", sheetDefinition.getName());
            final int lastHeaderRowIndex = sheetDefinition.getNumHeaderRows() - 1;

            int lastRowIndex = sheet.getLastRowNum();
            while (lastRowIndex > lastHeaderRowIndex) {
                final XSSFRow rowToDelete = sheet.getRow(lastRowIndex);
                sheet.removeRow(rowToDelete);
                lastRowIndex = sheet.getLastRowNum();
            }
        }
    }

    private static Map<String, CemiSheet> createSheetsMap(
            final CemiOutputDefinition outputDefinition, final SXSSFWorkbook workbook) {
        return outputDefinition.getSheets().stream()
                .collect(Collectors.toUnmodifiableMap(
                        CemiSheetDefinition::getName,
                        sheetDefinition -> new CemiSheet(
                                sheetDefinition, workbook.getSheet(sheetDefinition.getName()))
                ));
    }

    public void writeRow(final String sheetName, final String[] rowData) {
        Validate.notBlank(sheetName, "sheetName cannot be blank");
        Validate.notNull(rowData, "rowData cannot be null");

        final CemiSheet sheet = sheets.get(sheetName);
        Validate.validState(sheet != null, "Sheet not found: %s", sheetName);
        final int rowDataLength = sheet.sheetDefinition.getFields().size();
        Validate.validState(rowDataLength == rowData.length,
                "rowData for sheet %s should have had %s elements, but it actually had %s elements",
                sheetName, rowDataLength, rowData.length);

        final int nextRowIndex = sheet.workbookSheet.getLastRowNum() + 1;
        final SXSSFRow row = sheet.workbookSheet.createRow(nextRowIndex);
        final int columnOffset = sheet.sheetDefinition.getStartColumnIndex();

        for (int i = 0; i < rowDataLength; i++) {
            final String fieldValue = rowData[i];
            final SXSSFCell cell = row.createCell(i + columnOffset);
            if (StringUtils.isNotBlank(fieldValue)) {
                cell.setCellValue(fieldValue);
            }
        }
    }

    public void commit() throws IOException {
        Validate.validState(!committed, "The source spreadsheet file has already been overwritten");
        streamedWorkbook.write(fileOutputStream);
        committed = true;
    }

    @Override
    public void close() throws IOException {
        if (!committed) {
            LOG.warn("close, This instance has not yet committed its updates to the source spreadsheet file. "
                    + "Any changes that are only stored in auto-generated temporary files could be lost!");
        }
        IOUtils.closeQuietly(fileOutputStream, streamedWorkbook, templateWorkbook, opcPackage, fileInputStream);
    }

    private static final class CemiSheet {
        private final CemiSheetDefinition sheetDefinition;
        private final SXSSFSheet workbookSheet;

        private CemiSheet(final CemiSheetDefinition sheetDefinition, final SXSSFSheet workbookSheet) {
            Validate.notNull(sheetDefinition, "sheetDefinition cannot be null");
            Validate.notNull(workbookSheet, "workbookSheet cannot be null; sheet might not exist in file: %s",
                    sheetDefinition.getName());
            this.sheetDefinition = sheetDefinition;
            this.workbookSheet = workbookSheet;
        }
    }

}
