package edu.cornell.kfs.sys.batch.service.impl;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import edu.cornell.kfs.sys.batch.xml.CemiOutputDefinition;
import edu.cornell.kfs.sys.batch.xml.CemiSheetDefinition;
import edu.cornell.kfs.sys.util.CemiWorkbookCopier;

public class CemiExcelWriter implements Closeable {

    private static final Logger LOG = LogManager.getLogger();

    private final Map<String, CemiSheet> sheets;

    private OPCPackage opcPackage;
    private XSSFWorkbook templateWorkbook;
    private SXSSFWorkbook streamedWorkbook;
    private FileOutputStream fileOutputStream;
    private boolean committed;

    public CemiExcelWriter(final CemiOutputDefinition outputDefinition, final InputStream templateFileStream,
            final File targetFile) throws IOException, InvalidFormatException {
        Validate.notNull(outputDefinition, "outputDefinition cannot be null");
        Validate.notNull(templateFileStream, "templateFileStream cannot be null");
        Validate.notNull(targetFile, "targetFile cannot be null");
        boolean setupSucceeded = false;

        try {
            opcPackage = OPCPackage.open(templateFileStream);
            templateWorkbook = new XSSFWorkbook(opcPackage);

            final CemiWorkbookCopier workbookCopier = new CemiWorkbookCopier(outputDefinition);
            streamedWorkbook = workbookCopier.cleanAndCopyDataToStreamedWorkbook(templateWorkbook);

            this.sheets = createSheetsMap(outputDefinition, streamedWorkbook);
            fileOutputStream = new FileOutputStream(targetFile);
            committed = false;
            setupSucceeded = true;
        } finally {
            if (!setupSucceeded) {
                IOUtils.closeQuietly(fileOutputStream, streamedWorkbook, templateWorkbook, opcPackage);
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

        final int nextRowIndex = sheet.nextRowIndex.getAndAdd(1);
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
        Validate.validState(!committed, "The target spreadsheet file has already been created/overwritten");
        streamedWorkbook.write(fileOutputStream);
        committed = true;
    }

    @Override
    public void close() throws IOException {
        if (!committed) {
            LOG.warn("close, This instance has not yet committed its updates to the target spreadsheet file. "
                    + "Any changes that are only stored in auto-generated temporary files could be lost!");
        }
        IOUtils.closeQuietly(fileOutputStream, streamedWorkbook, templateWorkbook, opcPackage);
    }

    private static final class CemiSheet {
        private final CemiSheetDefinition sheetDefinition;
        private final SXSSFSheet workbookSheet;
        private final MutableInt nextRowIndex;

        private CemiSheet(final CemiSheetDefinition sheetDefinition, final SXSSFSheet workbookSheet) {
            Validate.notNull(sheetDefinition, "sheetDefinition cannot be null");
            Validate.notNull(workbookSheet, "workbookSheet cannot be null; sheet %s might not exist in file",
                    sheetDefinition.getName());
            this.sheetDefinition = sheetDefinition;
            this.workbookSheet = workbookSheet;
            this.nextRowIndex = new MutableInt(sheetDefinition.getNumHeaderRows());
        }
    }

}
