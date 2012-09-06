package edu.cornell.kfs.sys.batch;

import java.io.File;

import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.batch.BatchInputFileTypeBase;
import org.kuali.kfs.sys.exception.ParseException;

public class DocumentRequeuerFlatFileInputType extends BatchInputFileTypeBase {

	 private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(DocumentRequeuerFlatFileInputType.class);

	    private static final String FILE_NAME_PREFIX = "documentRequeue";
	
	public String getFileTypeIdentifer() {
		return "documentRequeuerFlatFileInputType";
	}

	public String getFileName(String principalName, Object parsedFileContents, String fileUserIdentifuer) {
		return FILE_NAME_PREFIX;
	}

	public Object parse(byte[] fileByteContent) throws ParseException {
		return "";
	}

	public boolean validate(Object parsedFileContents) {
		// TODO Auto-generated method stub
		return true;
	}

	public void process(String fileName, Object parsedFileContents) {
		// TODO Auto-generated method stub
		
	}

	public String getAuthorPrincipalName(File file) {
		// TODO Auto-generated method stub
        return "";
	}

	public String getTitleKey() {
		// TODO Auto-generated method stub
		return KFSKeyConstants.MESSAGE_BATCH_UPLOAD_TITLE_REQUEUER_FLAT_FILE;
	}
	
	

}
