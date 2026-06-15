package edu.cornell.kfs.cemi.sys.batch;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.batch.CsvBatchInputFileTypeBase;

import edu.cornell.kfs.sys.CUKFSConstants.FileExtensions;

@SuppressWarnings("rawtypes")
public class CemiCsvBatchInputFileType extends CsvBatchInputFileTypeBase {

    private String tableName;
    private boolean hasHeaderRow;

    public CemiCsvBatchInputFileType() {
        super();
        final String csvExtension = StringUtils.substringAfterLast(FileExtensions.CSV, KFSConstants.DELIMITER);
        setFileExtension(csvExtension);
    }

    @Override
    protected Object convertParsedObjectToVO(final Object parsedContent) {
        throw new UnsupportedOperationException("do not call");
    }

    @Override
    public String getFileName(final String principalName, final Object parsedFileContents,
            final String fileUserIdentifier) {
        throw new UnsupportedOperationException("do not call");
    }

    @Override
    public String getFileTypeIdentifier() {
        throw new UnsupportedOperationException("do not call");
    }

    @Override
    public boolean validate(final Object parsedFileContents) {
        throw new UnsupportedOperationException("do not call");
    }

    @Override
    public String getTitleKey() {
        throw new UnsupportedOperationException("do not call");
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(final String tableName) {
        this.tableName = tableName;
    }

    public boolean isHasHeaderRow() {
        return hasHeaderRow;
    }

    public void setHasHeaderRow(final boolean hasHeaderRow) {
        this.hasHeaderRow = hasHeaderRow;
    }

}
