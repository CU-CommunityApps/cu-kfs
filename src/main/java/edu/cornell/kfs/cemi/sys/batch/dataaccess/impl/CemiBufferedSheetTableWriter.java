package edu.cornell.kfs.cemi.sys.batch.dataaccess.impl;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.cornell.kfs.cemi.sys.CemiBaseConstants;
import edu.cornell.kfs.cemi.sys.batch.dataaccess.CemiSheetDao;
import edu.cornell.kfs.cemi.sys.batch.dataaccess.CemiTableMetadata;
import edu.cornell.kfs.cemi.sys.batch.service.CemiTableMetadataService;
import edu.cornell.kfs.cemi.sys.batch.xml.CemiOutputDefinition;

public class CemiBufferedSheetTableWriter implements Closeable {

    private static final Logger LOG = LogManager.getLogger();

    private final CemiSheetDao cemiSheetDao;
    private final Map<String, SheetBuffer> sheetBuffers;

    public CemiBufferedSheetTableWriter(final CemiSheetDao cemiSheetDao,
            final CemiTableMetadataService cemiTableMetadataService, final CemiOutputDefinition outputDefinition) {
        Validate.notNull(cemiSheetDao, "cemiSheetDao cannot be null");
        Validate.notNull(cemiTableMetadataService, "cemiTableMetadataService cannot be null");
        Validate.notNull(outputDefinition, "outputDefinition cannot be null");
        this.cemiSheetDao = cemiSheetDao;
        this.sheetBuffers = outputDefinition.getSheets().stream()
                .map(sheet -> cemiTableMetadataService.getCemiTableMetadata(outputDefinition, sheet.getName()))
                .collect(Collectors.toUnmodifiableMap(CemiTableMetadata::getSheetName, SheetBuffer::new));
    }

    public void write(final String sheetName, final Object rowObject) {
        final SheetBuffer sheetBuffer = sheetBuffers.get(sheetName);
        Validate.isTrue(sheetBuffer != null, "Unrecognized sheet name: %s", sheetName);
        Validate.notNull(rowObject, "rowObject cannot be null");
        sheetBuffer.rowObjects.add(sheetBuffer);
        if (sheetBuffer.rowObjects.size() >= CemiBaseConstants.SHEET_TABLE_BATCH_SIZE) {
            flushToDatabase(sheetBuffer);
        }
    }

    public void flushRows(final String sheetName) {
        final SheetBuffer sheetBuffer = sheetBuffers.get(sheetName);
        Validate.isTrue(sheetBuffer != null, "Unrecognized sheet name: %s", sheetName);
        if (sheetBuffer.rowObjects.size() > 0) {
            flushToDatabase(sheetBuffer);
        }
    }

    public void flushAllRows() {
        for (final SheetBuffer sheetBuffer : sheetBuffers.values()) {
            if (sheetBuffer.rowObjects.size() > 0) {
                flushToDatabase(sheetBuffer);
            }
        }
    }

    private void flushToDatabase(final SheetBuffer sheetBuffer) {
        cemiSheetDao.insertSheetTableRows(sheetBuffer.tableMetadata, sheetBuffer.rowObjects);
        sheetBuffer.rowObjects.clear();
    }

    @Override
    public void close() throws IOException {
        for (final SheetBuffer sheetBuffer : sheetBuffers.values()) {
            if (sheetBuffer.rowObjects.size() > 0) {
                LOG.warn("close, Buffered DTOs were still present for sheet {}; will discard them in case "
                        + "the closing was triggered by an exception", sheetBuffer.tableMetadata.getSheetName());
                sheetBuffer.rowObjects.clear();
            }
        }
    }

    private static final class SheetBuffer {
        private final CemiTableMetadata tableMetadata;
        private final List<Object> rowObjects;

        private SheetBuffer(final CemiTableMetadata tableMetadata) {
            this.tableMetadata = tableMetadata;
            this.rowObjects = new ArrayList<>(CemiBaseConstants.SHEET_TABLE_BATCH_SIZE);
        }
    }

}
