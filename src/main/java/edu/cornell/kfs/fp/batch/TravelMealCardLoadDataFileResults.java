package edu.cornell.kfs.fp.batch;

import org.kuali.kfs.sys.KFSConstants;

public class TravelMealCardLoadDataFileResults {
    
    protected boolean success;
    protected String nameOfFileLoaded;
    protected int numberOfLinesInFile;
    protected int numberOfLinesSuccessfullyRead;
    
    public TravelMealCardLoadDataFileResults() {
        success = false;
        nameOfFileLoaded = KFSConstants.EMPTY_STRING;
        numberOfLinesInFile = 0;
        numberOfLinesSuccessfullyRead = 0;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getNameOfFileLoaded() {
        return nameOfFileLoaded;
    }
    
    public void setNameOfFileLoaded(String nameOfFileLoaded) {
        this.nameOfFileLoaded = nameOfFileLoaded;
    }
    
    public int getNumberOfLinesInFile() {
        return numberOfLinesInFile;
    }
    
    public void setNumberOfLinesInFile(int numberOfLinesInFile) {
        this.numberOfLinesInFile = numberOfLinesInFile;
    }

    public int getNumberOfLinesSuccessfullyRead() {
        return numberOfLinesSuccessfullyRead;
    }

    public void setNumberOfLinesSuccessfullyRead(int numberOfLinesSuccessfullyRead) {
        this.numberOfLinesSuccessfullyRead = numberOfLinesSuccessfullyRead;
    }
}
