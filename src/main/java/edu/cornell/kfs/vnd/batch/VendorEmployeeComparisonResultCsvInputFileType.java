package edu.cornell.kfs.vnd.batch;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.batch.CsvBatchInputFileTypeBase;

import edu.cornell.kfs.vnd.CUVendorConstants;

public class VendorEmployeeComparisonResultCsvInputFileType
        extends CsvBatchInputFileTypeBase<VendorEmployeeComparisonResultCsv> {

    private static final Logger LOG = LogManager.getLogger();

    /**
     * This implementation doesn't need to use the value returned by this method, so it simply returns an empty string.
     */
    @Override
    public String getFileName(final String principalName, final Object parsedFileContents,
            final String fileUserIdentifier) {
        return KFSConstants.EMPTY_STRING;
    }

    @Override
    public String getFileTypeIdentifier() {
        return CUVendorConstants.VENDOR_EMPLOYEE_COMPARISON_RESULT_FILE_TYPE_ID;
    }

    @Override
    public boolean validate(final Object parsedFileContents) {
        return true;
    }

    @Override
    public String getAuthorPrincipalName(final File file) {
        return null;
    }

    /**
     * This file type is not meant for upload via the KFS UI, so this implementation returns an empty string.
     */
    @Override
    public String getTitleKey() {
        return KFSConstants.EMPTY_STRING;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Object convertParsedObjectToVO(final Object parsedContent) {
        try {
            final List<Map<String, String>> parsedCsvRows = (List<Map<String, String>>) parsedContent;
            return VendorEmployeeComparisonResultCsvBuilder.buildVendorEmployeeComparisonResults(parsedCsvRows);
        } catch (Exception e) {
            LOG.error("convertParsedObjectToVO, Failed to convert CSV rows into DTOs", e);
            throw new RuntimeException(e);
        }
    }

}
