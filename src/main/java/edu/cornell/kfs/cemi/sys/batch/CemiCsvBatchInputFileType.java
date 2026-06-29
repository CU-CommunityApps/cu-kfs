package edu.cornell.kfs.cemi.sys.batch;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.batch.BatchInputFileTypeBase;
import org.kuali.kfs.sys.exception.ParseException;
import org.springframework.beans.factory.BeanNameAware;

import edu.cornell.kfs.sys.CUKFSConstants.FileExtensions;

public class CemiCsvBatchInputFileType extends BatchInputFileTypeBase implements BeanNameAware {

    private String fileTypeName;
    private String legacyDataDestinationTableName;
    private List<String> legacyDataDestinationTableColumns;
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

    public String getFileTypeName() {
        return fileTypeName;
    }

    public void setFileTypeName(final String fileTypeName) {
        this.fileTypeName = fileTypeName;
    }

    @Override
    public void setBeanName(final String name) {
        setFileTypeName(name);        
    }

    public String getLegacyDataDestinationTableName() {
        return legacyDataDestinationTableName;
    }

    public void setLegacyDataDestinationTableName(final String legacyDataDestinationTableName) {
        this.legacyDataDestinationTableName = legacyDataDestinationTableName;
    }

    public List<String> getLegacyDataDestinationTableColumns() {
        return legacyDataDestinationTableColumns;
    }

    public void setLegacyDataDestinationTableColumns(final List<String> legacyDataDestinationTableColumns) {
        this.legacyDataDestinationTableColumns = legacyDataDestinationTableColumns;
    }

    public void setLegacyDataDestinationTableColumnsEnum(
            final Class<? extends Enum<?>> legacyDataDestinationTableColumnsEnum) {
        Validate.notNull(legacyDataDestinationTableColumnsEnum,
                "legacyDataDestinationTableColumnsEnum cannot be null");
        final List<String> columnNames = Arrays.stream(legacyDataDestinationTableColumnsEnum.getEnumConstants())
                .map(Object::toString)
                .collect(Collectors.toUnmodifiableList());
        setLegacyDataDestinationTableColumns(columnNames);
    }

    public boolean isHasHeaderRow() {
        return hasHeaderRow;
    }

    public void setHasHeaderRow(final boolean hasHeaderRow) {
        this.hasHeaderRow = hasHeaderRow;
    }

}
