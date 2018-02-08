package edu.cornell.kfs.sys.batch;

import org.kuali.kfs.sys.batch.FlatFileDataHandler;

public class KualiDeveloperFlatInputFileHandler implements FlatFileDataHandler {

    public boolean validate(Object parsedFileContents) {
        return false;
    }

    public void process(String fileName, Object parsedFileContents) {

    }

	public String getFileName(String principalName, Object parsedFileContents, String fileUserIdentifier) {
		return null;
	}

}
