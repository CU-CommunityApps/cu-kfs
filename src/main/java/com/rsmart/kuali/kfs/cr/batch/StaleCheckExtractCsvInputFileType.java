package com.rsmart.kuali.kfs.cr.batch;

import com.rsmart.kuali.kfs.cr.CRConstants;
import com.rsmart.kuali.kfs.cr.CRKeyConstants;
import edu.cornell.kfs.pdp.CUPdpConstants;
import edu.cornell.kfs.pdp.batch.PayeeACHAccountExtractCsv;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.batch.CsvBatchInputFileTypeBase;
import org.kuali.kfs.sys.exception.ParseException;

import java.io.File;
import java.util.List;
import java.util.Map;

public class StaleCheckExtractCsvInputFileType extends CsvBatchInputFileTypeBase<PayeeACHAccountExtractCsv> {
	private static final Logger LOG = LogManager.getLogger(StaleCheckExtractCsvInputFileType.class);

    /**
     * This implementation just returns an empty string, since the returned value is not needed in this case.
     *
     * @see org.kuali.kfs.sys.batch.BatchInputFileType#getFileName(String, Object, String)
     */
    @Override
    public String getFileName(String principalName, Object parsedFileContents, String fileUserIdentifier) {
        return KFSConstants.EMPTY_STRING;
    }

    @Override
    public String getFileTypeIdentifer() {
        return CRConstants.STALE_CHECK_EXTRACT_FILE_TYPE_ID;
    }

    @Override
    public boolean validate(Object parsedFileContents) {
        return true;
    }

    @Override
    public String getAuthorPrincipalName(File file) {
        return null;
    }

    @Override
    public String getTitleKey() {
        return CRKeyConstants.MESSAGE_BATCH_UPLOAD_TITLE_STALE_CHECK;
    }

    /**
     * Overridden to call "convertParsedObjectToVO" after performing the regular parsing.
     *
     * @see CsvBatchInputFileTypeBase#parse(byte[])
     */
    @Override
    public Object parse(byte[] fileByteContent) throws ParseException {
        Object parsedContents = super.parse(fileByteContent);
        return convertParsedObjectToVO(parsedContents);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Object convertParsedObjectToVO(Object parsedContent) {
        // Convert from List<Map<String,String>> to a list of DTOs, and catch errors appropriately as in similar parsing methods.
        try {
            return StaleCheckExtractCsvBuilder.buildStaleCheckExtract((List<Map<String,String>>) parsedContent);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

}
