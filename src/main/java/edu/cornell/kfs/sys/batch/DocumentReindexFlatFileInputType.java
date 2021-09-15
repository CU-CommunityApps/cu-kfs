package edu.cornell.kfs.sys.batch;

import java.io.File;

import org.kuali.kfs.sys.exception.ParseException;

import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.CUKFSKeyConstants;

public class DocumentReindexFlatFileInputType extends CuBatchInputFileTypeBase {

	@Override
	public String getFileTypeIdentifier() {
		return "documentReindexFlatFileInputType";
	}

	@Override
	public String getFileName(String principalName, Object parsedFileContents, String fileUserIdentifuer) {
		return CUKFSConstants.DOCUMENT_REINDEX_FILE_NAME_PREFIX;
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
