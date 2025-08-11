package edu.cornell.kfs.fp.batch;

import java.io.File;
import java.util.ArrayList;

import org.kuali.kfs.sys.batch.FlatFileParserBase;
import org.kuali.kfs.sys.exception.ParseException;

import edu.cornell.kfs.fp.businessobject.TravelMealCardFileLineEntry;


public class TravelMealCardFlatInputFileType extends FlatFileParserBase {

    protected ArrayList<String> errorMessages;
    protected int lineCount = 1;

    public String getFileTypeIdentifer() {
        return "travelMealCardFlatInputFileType";
    }

    public Object parse(byte[] fileByteContent) throws ParseException {
        ArrayList<TravelMealCardFileLineEntry> tmCardFileLines = new ArrayList<TravelMealCardFileLineEntry>();
        tmCardFileLines = (ArrayList<TravelMealCardFileLineEntry>) super.parse(fileByteContent);
        return tmCardFileLines;
    }

    public boolean validate(Object parsedFileContents) {
        return false;
    }

    public void process(String fileName, Object parsedFileContents) {
    }

    public String getAuthorPrincipalName(File file) {
        return null;
    }

    public String getTitleKey() {
        return null;
    }

}
