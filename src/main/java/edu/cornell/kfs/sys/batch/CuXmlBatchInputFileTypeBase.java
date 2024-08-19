package edu.cornell.kfs.sys.batch;

import static org.kuali.kfs.pdp.PdpConstants.FILE_NAME_PART_DELIMITER;

import java.io.ByteArrayInputStream;
import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.batch.XmlBatchInputFileTypeBase;
import org.kuali.kfs.sys.exception.ParseException;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.ValidationEventHandler;

public abstract class CuXmlBatchInputFileTypeBase<T> extends XmlBatchInputFileTypeBase<T> {

    private static final Logger LOG = LogManager.getLogger();

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

    /**
     * Custom variant of the base financials parse() method that allows for configuring a Listener
     * and/or ValidationEventHandler on the Unmarshaller. If KualiCo ever adjusts its base parse() method
     * to make it easier to inject such configuration, this custom method should be removed.
     */
    @SuppressWarnings("unchecked")
    protected T parse(final byte[] fileByteContent, final Object listener) {
        if (fileByteContent == null || listener == null) {
            LOG.error("an invalid(null) argument was given");
            throw new IllegalArgumentException("an invalid(null) argument was given");
        } else if (!(listener instanceof Unmarshaller.Listener) || !(listener instanceof ValidationEventHandler)) {
            LOG.error("an invalid argument was given, unsupported listener/handler implementation");
            throw new IllegalArgumentException(
                    "an invalid argument was given, unsupported listener/handler implementation");
        }

        // handle zero byte contents, xml parsers don't deal with them well
        if (fileByteContent.length == 0) {
            LOG.error("an invalid argument was given, empty input stream");
            throw new IllegalArgumentException("an invalid argument was given, empty input stream");
        }

        // validate contents against schema
        final ByteArrayInputStream validateFileContents = new ByteArrayInputStream(fileByteContent);
        validateContentsAgainstSchema(schemaLocation, validateFileContents);

        try {
            final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(fileByteContent);
            final JAXBContext jaxbContext = JAXBContext.newInstance(getOutputClass());
            final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            if (listener instanceof Unmarshaller.Listener) {
                jaxbUnmarshaller.setListener((Unmarshaller.Listener) listener);
            }
            if (listener instanceof ValidationEventHandler) {
                jaxbUnmarshaller.setEventHandler((ValidationEventHandler) listener);
            }

            return (T) jaxbUnmarshaller.unmarshal(byteArrayInputStream);

        } catch (final JAXBException e) {
            LOG.error("Error parsing xml contents", e);
            throw new ParseException("Error parsing xml contents: " + e.getMessage(), e);
        }
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
