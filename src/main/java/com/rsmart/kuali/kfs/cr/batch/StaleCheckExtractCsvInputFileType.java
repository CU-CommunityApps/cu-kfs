package com.rsmart.kuali.kfs.cr.batch;

import com.rsmart.kuali.kfs.cr.CRConstants;
import com.rsmart.kuali.kfs.cr.CRKeyConstants;
import com.rsmart.kuali.kfs.cr.businessobject.StaleCheckBatchRow;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.batch.CsvBatchInputFileTypeBase;
import org.kuali.kfs.sys.exception.ParseException;

import java.io.File;
import java.util.List;
import java.util.Map;

public class StaleCheckExtractCsvInputFileType extends CsvBatchInputFileTypeBase<StaleCheckExtractCsvFields> {
	private static final Logger LOG = LogManager.getLogger(StaleCheckExtractCsvInputFileType.class);

    @Override
    public String getFileName(String principalName, Object parsedFileContents, String userIdentifier) {
        String fileName = "cr_stale_" + principalName;
        if (StringUtils.isNotBlank(userIdentifier)) {
            fileName += "_" + userIdentifier;
        }
        fileName = StringUtils.remove(fileName, KFSConstants.BLANK_SPACE);
        return fileName;
    }

    @Override
    public String getFileTypeIdentifier() {
        return CRConstants.STALE_CHECK_EXTRACT_FILE_TYPE_ID;
    }

    @Override
    public boolean validate(Object parsedFileContents) {
        return true;
    }

    @Override
    public String getAuthorPrincipalName(File file) {
        return StringUtils.EMPTY;
    }

    @Override
    public String getTitleKey() {
        return CRKeyConstants.MESSAGE_BATCH_UPLOAD_TITLE_STALE_CHECK;
    }

    @Override
    public Object parse(byte[] fileByteContent) throws ParseException {
        Object parsedContents = super.parse(fileByteContent);
        return convertParsedObjectToVO(parsedContents);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<StaleCheckBatchRow> convertParsedObjectToVO(Object parsedContent) {
        try {
            return StaleCheckExtractCsvBuilder.buildStaleCheckExtractList((List<Map<String,String>>) parsedContent);
        } catch (Exception e) {
            LOG.error("convertParsedObjectToVO: " + e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

}
