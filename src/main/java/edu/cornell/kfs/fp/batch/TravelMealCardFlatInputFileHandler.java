package edu.cornell.kfs.fp.batch;

import org.kuali.kfs.sys.batch.FlatFileDataHandler;

public class TravelMealCardFlatInputFileHandler implements FlatFileDataHandler {

    public boolean validate(Object parsedFileContents) {
        return false;
    }

    public void process(String fileName, Object parsedFileContents) {
    }

    public String getFileName(String principalName, Object parsedFileContents, String fileUserIdentifier) {
        return null;
    }

}
