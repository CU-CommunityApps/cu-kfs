package edu.cornell.kfs.sys.batch;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.batch.BatchInputFileTypeBase;
import org.kuali.kfs.sys.exception.ParseException;
import org.kuali.rice.core.api.datetime.DateTimeService;

import edu.cornell.kfs.sys.service.CUMarshalService;

/**
 * Alternative to XmlBatchInputFileTypeBase that parses XML POJOs using JAXB instead.
 * 
 * Similarly to FlatFileParserBase, this implementation has properties to configure
 * the File Type Identifier and Title Key, and also constructs file names using
 * the same pattern as that class (and has a property for the file name prefix).
 */
public class JAXBXmlBatchInputFileTypeBase extends BatchInputFileTypeBase {

    private static final String FILE_NAME_PART_DELIMITER = "_";

    protected DateTimeService dateTimeService;
    protected CUMarshalService marshalService;
    protected String fileTypeIdentifier;
    protected String titleKey;
    protected String fileNamePrefix;
    protected Class<?> pojoClass;

    @Override
    public String getFileName(String principalName, Object parsedFileContents, String fileUserIdentifier) {
        Date currentDate = dateTimeService.getCurrentDate();
        String fileName = new StringBuilder()
                .append(fileNamePrefix)
                .append(principalName)
                .append(buildFileNameFragmentFromFileUserIdentifier(fileUserIdentifier))
                .append(FILE_NAME_PART_DELIMITER).append(dateTimeService.toDateTimeStringForFilename(currentDate))
                .toString();
        
        return StringUtils.remove(fileName, KFSConstants.BLANK_SPACE);
    }

    protected String buildFileNameFragmentFromFileUserIdentifier(String fileUserIdentifier) {
        return StringUtils.isNotBlank(fileUserIdentifier)
                ? FILE_NAME_PART_DELIMITER + fileUserIdentifier
                : StringUtils.EMPTY;
    }

    @Override
    public String getFileTypeIdentifer() {
        return fileTypeIdentifier;
    }

    @Override
    public Object parse(byte[] fileByteContent) throws ParseException {
        try {
            String fileStringContent = new String(fileByteContent, StandardCharsets.UTF_8);
            return marshalService.unmarshalString(fileStringContent, pojoClass);
        } catch (JAXBException e) {
            throw new ParseException("Error attempting to unmarshal POJO from XML", e);
        }
    }

    @Override
    public void process(String fileName, Object parsedFileContents) {
        // Do nothing by default.
    }

    @Override
    public boolean validate(Object parsedFileContents) {
        return true;
    }

    @Override
    public String getAuthorPrincipalName(File file) {
        return StringUtils.substringBetween(file.getName(), fileNamePrefix, FILE_NAME_PART_DELIMITER);
    }

    @Override
    public String getTitleKey() {
        return titleKey;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    public void setMarshalService(CUMarshalService marshalService) {
        this.marshalService = marshalService;
    }

    public void setFileTypeIdentifier(String fileTypeIdentifier) {
        this.fileTypeIdentifier = fileTypeIdentifier;
    }

    public void setTitleKey(String titleKey) {
        this.titleKey = titleKey;
    }

    public void setFileNamePrefix(String fileNamePrefix) {
        this.fileNamePrefix = fileNamePrefix;
    }

    public void setPojoClass(Class<?> pojoClass) {
        this.pojoClass = pojoClass;
    }

}
