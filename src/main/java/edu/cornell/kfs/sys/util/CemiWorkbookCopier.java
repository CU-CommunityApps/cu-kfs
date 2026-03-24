package edu.cornell.kfs.sys.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.compress.archivers.zip.Zip64Mode;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import edu.cornell.kfs.sys.batch.xml.CemiOutputDefinition;
import edu.cornell.kfs.sys.batch.xml.CemiSheetDefinition;

public class CemiWorkbookCopier {

    private static final Logger LOG = LogManager.getLogger();

    private final CemiOutputDefinition outputDefinition;
    private final Map<String, RowData[]> rowDataMappings;

    private boolean performedCopy;

    public CemiWorkbookCopier(final CemiOutputDefinition outputDefinition) {
        Validate.notNull(outputDefinition, "outputDefinition cannot be null");
        this.outputDefinition = outputDefinition;
        this.rowDataMappings = new HashMap<>();
    }

    public SXSSFWorkbook cleanAndCopyDataToStreamedWorkbook(final XSSFWorkbook oldWorkbook) {
        Validate.validState(!performedCopy, "The copying operation has already been performed on this instance");
        SXSSFWorkbook newWorkbook = null;
        boolean success = false;

        try {
            trackHeaderRowDataAndDeleteRowsOnModifiableSheets(oldWorkbook);

            newWorkbook = new SXSSFWorkbook(oldWorkbook);
            disableZip64ModeToImproveCompatibility(newWorkbook);
            reinsertHeaderRowsIntoStreamedWorkbook(newWorkbook);
            
            performedCopy = true;
            success = true;
            return newWorkbook;
        } finally {
            if (!success) {
                IOUtils.closeQuietly(newWorkbook);
            }
        }
    }

    private void trackHeaderRowDataAndDeleteRowsOnModifiableSheets(final XSSFWorkbook oldWorkbook) {
        for (final CemiSheetDefinition sheetDefinition : outputDefinition.getSheets()) {
            final String sheetName = sheetDefinition.getName();
            final int headerRowCount = CemiUtils.getHeaderRowCount(sheetDefinition);
            final int columnCount = CemiUtils.getFullColumnCount(sheetDefinition);
            final RowData[] rowsToReinsert = new RowData[headerRowCount];
            final XSSFSheet sheet = oldWorkbook.getSheet(sheetName);
            Validate.validState(sheet != null, "The %s sheet was not found in the workbook", sheetName);
            Validate.validState(sheet.getLastRowNum() >= headerRowCount - 1, "The %s sheet had less than %s rows",
                    sheetName, headerRowCount);

            for (int rowIndex = sheet.getLastRowNum(); rowIndex >= 0; rowIndex--) {
                final XSSFRow row = sheet.getRow(rowIndex);
                if (rowIndex >= headerRowCount) {
                    if (row != null) {
                        sheet.removeRow(row);
                    }
                    continue;
                }

                Validate.validState(row != null, "Row index %s on sheet %s does not exist", rowIndex, sheetName);
                final RowData rowData = new RowData(row, columnCount);
                rowsToReinsert[rowIndex] = rowData;

                sheet.removeRow(row);
            }

            rowDataMappings.put(sheetName, rowsToReinsert);
        }
    }

    /*
     * NOTE: Apache POI's SXSSFWorkbook doesn't seem to be setting up the ZIP64 headers properly,
     * so we need to disable ZIP64 mode to make the spreadsheet compatible with other tools.
     * However, doing so will limit the size of the spreadsheet contents to 4GB. If we end up needing
     * to prepare content larger than 4GB, then we may have to implement an alternative solution
     * (such as programmatically unzipping and re-zipping the files to fix the headers).
     */
    private void disableZip64ModeToImproveCompatibility(final SXSSFWorkbook newWorkbook) {
        newWorkbook.setZip64Mode(Zip64Mode.Never);
    }

    private void reinsertHeaderRowsIntoStreamedWorkbook(final SXSSFWorkbook newWorkbook) {
        for (final CemiSheetDefinition sheetDefinition : outputDefinition.getSheets()) {
            final String sheetName = sheetDefinition.getName();
            final SXSSFSheet newSheet = newWorkbook.getSheet(sheetName);
            final RowData[] rows = rowDataMappings.get(sheetName);
            final int rowCount = CemiUtils.getHeaderRowCount(sheetDefinition);
            final int columnCount = CemiUtils.getFullColumnCount(sheetDefinition);
            Validate.validState(newSheet != null, "Sheet %s not found in streaming workbook", sheetName);
            Validate.validState(rows != null, "Row data for Sheet %s not found", sheetName);

            for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
                final RowData rowData = rows[rowIndex];
                if (rowData == null) {
                    continue;
                }

                final SXSSFRow newRow = newSheet.createRow(rowIndex);
                newRow.setHeightInPoints(rowData.heightInPoints);
                newRow.setZeroHeight(rowData.zeroHeight);
                if (rowData.rowStyle != null) {
                    newRow.setRowStyle(rowData.rowStyle);
                }

                for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
                    final CellData cellData = rowData.cells[columnIndex];
                    if (cellData != null) {
                        final SXSSFCell newCell = newRow.createCell(columnIndex);
                        newCell.setCellStyle(cellData.cellStyle);
                        populateCellValue(cellData, newCell);
                    }
                }
            }
        }
    }

    private void populateCellValue(final CellData cellData, final SXSSFCell newCell) {
        switch (cellData.cellType) {
            case BLANK :
                break;

            case STRING :
                newCell.setCellValue((String) cellData.cellValue);
                break;

            case NUMERIC :
                newCell.setCellValue((Double) cellData.cellValue);
                break;

            case BOOLEAN :
                newCell.setCellValue((Boolean) cellData.cellValue);
                break;

            default :
                LOG.warn("populateCellValue, Found a header cell at row index {} and column index {} "
                        + "with a cell type of {}; defaulting to a blank cell instead",
                        newCell.getRowIndex(), newCell.getColumnIndex(), cellData.cellType);
                break;
        }
    }

    private static final class RowData {
        private final float heightInPoints;
        private final boolean zeroHeight;
        private final CellStyle rowStyle;
        private final CellData[] cells;

        private RowData(final XSSFRow row, final int columnCount) {
            this.heightInPoints = row.getHeightInPoints();
            this.zeroHeight = row.getZeroHeight();
            this.rowStyle = row.isFormatted() ? row.getRowStyle() : null;
            this.cells = new CellData[columnCount];

            for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
                final XSSFCell cell = row.getCell(columnIndex);
                if (cell != null) {
                    cells[columnIndex] = new CellData(cell);
                }
            }
        }
    }

    private static final class CellData {
        private final CellType cellType;
        private final CellStyle cellStyle;
        private final Object cellValue;

        private CellData(final XSSFCell cell) {
            this.cellType = cell.getCellType();
            this.cellStyle = cell.getCellStyle();
            this.cellValue = getCellValue(cell);
        }

        private static Object getCellValue(final XSSFCell cell) {
            switch (cell.getCellType()) {
                case BLANK :
                    return null;

                case STRING :
                    return cell.getStringCellValue();

                case NUMERIC :
                    return cell.getNumericCellValue();

                case BOOLEAN :
                    return cell.getBooleanCellValue();

                default :
                    return null;
            }
        }
    }

}
