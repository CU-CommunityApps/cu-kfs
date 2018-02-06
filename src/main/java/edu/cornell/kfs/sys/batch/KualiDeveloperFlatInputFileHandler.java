package edu.cornell.kfs.sys.batch;

import org.kuali.kfs.sys.batch.FlatFileDataHandler;

public class KualiDeveloperFlatInputFileHandler implements FlatFileDataHandler {

    /**
     * @see FlatFileDataHandler#validate(Object)
     */
    public boolean validate(Object parsedFileContents) {
        return false;
    }

    /**
     * @see FlatFileDataHandler#process(String, Object)
     */
    public void process(String fileName, Object parsedFileContents) {

    }

    // TODO UPGRADE-911
	public String getFileName(String principalName, Object parsedFileContents,
			String fileUserIdentifier) {
		return null;
	}

}
