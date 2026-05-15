package edu.cornell.kfs.cemi.sys.batch.service.impl;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.function.TriConsumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.XMLHelper;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.kuali.kfs.sys.KFSConstants;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import edu.cornell.kfs.cemi.sys.batch.xml.CemiOutputDefinition;
import edu.cornell.kfs.cemi.sys.batch.xml.CemiSheetDefinition;

/**
 * Helper class that uses Apache POI's "event user model" to simplify the streamed reading of Excel workbook content.
 * This is based upon the following example implementation:
 * 
 * https://github.com/apache/poi/blob/trunk/poi-examples/src/main/java/org/apache/poi/examples/xssf/eventusermodel/XLSX2CSV.java
 */
public class CemiExcelReader implements Closeable {

    private static final Logger LOG = LogManager.getLogger();

    private final ReadOnlySharedStringsTable sharedStringsTable;
    private final Map<String, CemiSheetHandler> cemiSheetHandlers;

    private OPCPackage opcPackage;

    public CemiExcelReader(final File workbookFile, final CemiOutputDefinition outputDefinition,
            final TriConsumer<String, Integer, String[]> rowDataHandler)
                    throws IOException, InvalidFormatException, SAXException {
        boolean setupSucceeded = false;

        try {
            opcPackage = OPCPackage.open(workbookFile, PackageAccess.READ);
            this.sharedStringsTable = new ReadOnlySharedStringsTable(opcPackage);
            this.cemiSheetHandlers = outputDefinition.getSheets().stream()
                    .collect(Collectors.toUnmodifiableMap(
                            CemiSheetDefinition::getName, sheet -> new CemiSheetHandler(sheet, rowDataHandler)));

            setupSucceeded = true;
        } finally {
            if (!setupSucceeded) {
                IOUtils.closeQuietly(opcPackage);
            }
        }
    }

    public void parse() throws IOException, OpenXML4JException, ParserConfigurationException, SAXException {
        final XSSFReader xssfReader = new XSSFReader(opcPackage);
        final StylesTable stylesTable = xssfReader.getStylesTable();
        final XSSFReader.SheetIterator sheetIterator = xssfReader.getSheetIterator();

        while (sheetIterator.hasNext()) {
            try (
                final InputStream sheetStream = sheetIterator.next();
            ) {
                final String sheetName = sheetIterator.getSheetName();
                final CemiSheetHandler cemiSheetHandler = cemiSheetHandlers.get(sheetName);
                if (cemiSheetHandler == null) {
                    LOG.info("parse, Skipping unmapped sheet: {}", sheetName);
                    continue;
                } else {
                    LOG.info("parse, Reading sheet: {}", sheetName);
                }

                final XMLReader xmlSheetReader = XMLHelper.newXMLReader();
                final InputSource sheetInputSource = new InputSource(sheetStream);
                final XSSFSheetXMLHandler sheetContentHandler = new XSSFSheetXMLHandler(
                        stylesTable, sharedStringsTable, cemiSheetHandler, false);
                xmlSheetReader.setContentHandler(sheetContentHandler);
                xmlSheetReader.parse(sheetInputSource);
            }
        }
    }

    @Override
    public void close() throws IOException {
        IOUtils.closeQuietly(opcPackage);
    }

    private static final class CemiSheetHandler implements XSSFSheetXMLHandler.SheetContentsHandler {

        private final CemiSheetDefinition sheetDefinition;
        private final TriConsumer<String, Integer, String[]> rowDataHandler;
        private final int lastColumnIndex;

        private int currentRowIndex;
        private short currentColumnIndex;
        private Stream.Builder<String> currentDataRowContent;

        private CemiSheetHandler(final CemiSheetDefinition sheetDefinition,
                final TriConsumer<String, Integer, String[]> rowDataHandler) {
            this.sheetDefinition = sheetDefinition;
            this.rowDataHandler = rowDataHandler;
            this.lastColumnIndex = sheetDefinition.getStartColumnIndex() + sheetDefinition.getFields().size() - 1;
        }

        @Override
        public void startRow(final int rowNum) {
            currentRowIndex = rowNum;
            currentColumnIndex = - 1;
            if (currentRowIndex >= sheetDefinition.getNumHeaderRows()) {
                currentDataRowContent = Stream.builder();
            }
        }

        @Override
        public void cell(final String cellReference, final String formattedValue, final XSSFComment comment) {
            final CellReference cellReferenceToUse;
            if (currentRowIndex < sheetDefinition.getNumHeaderRows()) {
                return;
            } else if (StringUtils.isBlank(cellReference)) {
                final CellAddress cellAddress = new CellAddress(currentRowIndex, currentColumnIndex + 1);
                cellReferenceToUse = new CellReference(cellAddress.formatAsString());
            } else {
                cellReferenceToUse = new CellReference(cellReference);
            }

            final short newColumnIndex = cellReferenceToUse.getCol();
            while (currentColumnIndex < newColumnIndex - 1) {
                appendAndIncrementRowContent(KFSConstants.EMPTY_STRING);
            }

            final String cellValue = StringUtils.defaultString(formattedValue);
            appendAndIncrementRowContent(cellValue);
        }

        private void appendAndIncrementRowContent(final String cellValue) {
            currentDataRowContent.add(cellValue);
            currentColumnIndex++;
        }

        @Override
        public void endRow(final int rowNum) {
            Validate.validState(rowNum == currentRowIndex, "Processing wrong row; expected: %s, actual: %s",
                    currentRowIndex, rowNum);
            if (currentRowIndex >= sheetDefinition.getNumHeaderRows()) {
                while (currentColumnIndex < lastColumnIndex) {
                    appendAndIncrementRowContent(KFSConstants.EMPTY_STRING);
                }
                final int dataRowIndex = currentRowIndex - sheetDefinition.getNumHeaderRows();
                final String[] rowContent = currentDataRowContent.build()
                        .skip(sheetDefinition.getStartColumnIndex())
                        .limit(sheetDefinition.getFields().size())
                        .toArray(String[]::new);
                rowDataHandler.accept(sheetDefinition.getName(), dataRowIndex, rowContent);
            }
        }

    }

}
