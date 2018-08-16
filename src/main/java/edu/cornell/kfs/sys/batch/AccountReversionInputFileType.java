package edu.cornell.kfs.sys.batch;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.exception.ParseException;

import edu.cornell.kfs.sys.CUKFSKeyConstants;

public class AccountReversionInputFileType extends CuBatchInputFileTypeBase {

	private static final Logger LOG = LogManager.getLogger(AccountReversionInputFileType.class);

    private static final String FILE_NAME_PREFIX = "AccountReversion";

    /**
     * @see org.kuali.kfs.sys.batch.BatchInputFileType#getFileTypeIdentifer()
     */
    @Override
    public String getFileTypeIdentifer() {
        return "accountReversionInputFileType";
    }

    /**
     * @see org.kuali.kfs.sys.batch.BatchInputFileType#getFileName(java.lang.String, java.lang.Object, java.lang.String)
     */
    @Override
    public String getFileName(String principalName, Object parsedFileContents, String fileUserIdentifuer) {
        return FILE_NAME_PREFIX;
    }

    /**
     * @see org.kuali.kfs.sys.batch.BatchInputFileType#parse(byte[])
     */
    @Override
    public Object parse(byte[] fileByteContent) throws ParseException {
        return "";
    }

    /**
     * @see org.kuali.kfs.sys.batch.BatchInputFileType#validate(java.lang.Object)
     */
    @Override
    public boolean validate(Object parsedFileContents) {
        return true;
    }

    /**
     * @see org.kuali.kfs.sys.batch.BatchInputFileType#process(java.lang.String, java.lang.Object)
     */
    @Override
    public void process(String fileName, Object parsedFileContents) {

    }

    /**
     * @see org.kuali.kfs.sys.batch.BatchInputType#getAuthorPrincipalName(java.io.File)
     */
    @Override
    public String getAuthorPrincipalName(File file) {
        return "";
    }

    /**
     * @see org.kuali.kfs.sys.batch.BatchInputType#getTitleKey()
     */
    @Override
    public String getTitleKey() {
        return CUKFSKeyConstants.MESSAGE_BATCH_UPLOAD_TITLE_ACCOUNT_REVERSION_FLAT_FILE;
    }

    /**
     * Don't generate a .done file when uploading an account reversion file.
     * 
     * @see org.kuali.kfs.sys.batch.BatchInputFileTypeBase#isDoneFileRequired()
     */
    
    @Override
    public boolean isDoneFileRequired() {
        return false;
    }

}
