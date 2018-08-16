package edu.cornell.kfs.sys.batch;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.exception.ParseException;

import edu.cornell.kfs.sys.CUKFSKeyConstants;

public class DocumentReindexFlatFileInputType extends CuBatchInputFileTypeBase {

	private static final Logger LOG = LogManager.getLogger(DocumentReindexFlatFileInputType.class);
	private static final String FILE_NAME_PREFIX = "documentReindex";
	
	@Override
	public String getFileTypeIdentifer() {
		return "documentReindexFlatFileInputType";
	}

	@Override
	public String getFileName(String principalName, Object parsedFileContents, String fileUserIdentifuer) {
		return FILE_NAME_PREFIX;
	}

	@Override
	public Object parse(byte[] fileByteContent) throws ParseException {
		return "";
	}

	@Override
	public boolean validate(Object parsedFileContents) {
		return true;
	}

	@Override
	public void process(String fileName, Object parsedFileContents) {		
	}
	
	@Override
	public String getAuthorPrincipalName(File file) {
        return "";
	}
	
	@Override
	public String getTitleKey() {
		return CUKFSKeyConstants.MESSAGE_BATCH_UPLOAD_TITLE_REINDEX_FLAT_FILE;
	}
	
	@Override
    public boolean isDoneFileRequired() {
    	return false;
    }
}
