package edu.cornell.kfs.cemi.sys.batch;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.batch.BatchInputFileTypeBase;
import org.kuali.kfs.sys.exception.ParseException;

import edu.cornell.kfs.sys.CUKFSConstants.FileExtensions;

public class CemiCsvBatchInputFileType extends BatchInputFileTypeBase {

    private Class<?> csvEnumClass;
    private String tableName;
    private boolean hasHeaderRow;

    public CemiCsvBatchInputFileType() {
        super();
        final String csvExtension = StringUtils.substringAfterLast(FileExtensions.CSV, KFSConstants.DELIMITER);
        setFileExtension(csvExtension);
    }

    @Override
    public String getFileName(final String principalName, final Object parsedFileContents,
            final String fileUserIdentifier) {
        throw createUnsupportedOperationException();
    }

    @Override
    public String getFileTypeIdentifier() {
        throw createUnsupportedOperationException();
    }

    @Override
    public Object parse(final byte[] fileByteContent) throws ParseException {
        throw createUnsupportedOperationException();
    }

    @Override
    public void process(final String fileName, final Object parsedFileContents) {
        throw createUnsupportedOperationException();
    }

    @Override
    public boolean validate(final Object parsedFileContents) {
        throw createUnsupportedOperationException();
    }

    @Override
    public String getAuthorPrincipalName(final File file) {
        throw createUnsupportedOperationException();
    }

    @Override
    public String getTitleKey() {
        throw createUnsupportedOperationException();
    }

    private UnsupportedOperationException createUnsupportedOperationException() {
        return new UnsupportedOperationException("This method is not meant to be used by this implementation");
    }

    public Class<?> getCsvEnumClass() {
        return csvEnumClass;
    }

    public void setCsvEnumClass(final Class<?> csvEnumClass) {
        this.csvEnumClass = csvEnumClass;
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
