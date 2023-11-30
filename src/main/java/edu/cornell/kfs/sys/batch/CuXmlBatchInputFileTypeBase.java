package edu.cornell.kfs.sys.batch;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.batch.XmlBatchInputFileTypeBase;

import java.io.File;

import static org.kuali.kfs.pdp.PdpConstants.FILE_NAME_PART_DELIMITER;

public abstract class CuXmlBatchInputFileTypeBase<T> extends XmlBatchInputFileTypeBase<T> {

    protected String fileNamePrefix;
    protected DateTimeService dateTimeService;

    @Override
    public boolean validate(final Object parsedFileContents) {
        return true;
    }

    @Override
    public String getAuthorPrincipalName(final File file) {
        String filename = file.getName();
        if (StringUtils.isBlank(filename) || !filename.contains(FILE_NAME_PART_DELIMITER) || !filename.contains(fileNamePrefix)) {
            return StringUtils.EMPTY;
        }

        return StringUtils.substringBetween(filename, fileNamePrefix, FILE_NAME_PART_DELIMITER);
    }

    @Override
    public String getFileName(String principalName, Object parsedFileContents, String fileUserIdentifier) {
        String fileName = new StringBuilder()
                .append(fileNamePrefix)
                .append(principalName)
                .append(StringUtils.isBlank(fileUserIdentifier) ? StringUtils.EMPTY : FILE_NAME_PART_DELIMITER + fileUserIdentifier)
                .append(FILE_NAME_PART_DELIMITER)
                .append(dateTimeService.toDateTimeStringForFilename(dateTimeService.getCurrentDate()))
                .toString();

        return StringUtils.remove(fileName, KFSConstants.BLANK_SPACE);
    }

    public String getFileNamePrefix() {
        return fileNamePrefix;
    }

    public void setFileNamePrefix(String fileNamePrefix) {
        this.fileNamePrefix = fileNamePrefix;
    }

    public DateTimeService getDateTimeService() {
        return dateTimeService;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

}
