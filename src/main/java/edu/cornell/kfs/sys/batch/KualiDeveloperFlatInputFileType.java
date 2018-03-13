package edu.cornell.kfs.sys.batch;

import java.io.File;
import java.util.ArrayList;

import org.kuali.kfs.sys.batch.FlatFileParserBase;
import org.kuali.kfs.sys.exception.ParseException;

import edu.cornell.kfs.sys.businessobject.KualiDeveloper;

public class KualiDeveloperFlatInputFileType extends FlatFileParserBase {

    protected ArrayList<String> errorMessages;

    public String getFileTypeIdentifer() {
        return "kualiDeveloperFlatInputFileType";
    }

    public Object parse(byte[] fileByteContent) throws ParseException {
        ArrayList<KualiDeveloper> kualiDeveloperEntries = (ArrayList<KualiDeveloper>) super.parse(fileByteContent);
        return kualiDeveloperEntries;
    }

    public boolean validate(Object parsedFileContents) {
        return false;
    }

    public void process(String fileName, Object parsedFileContents) {
        System.out.println("HIT");
    }

    public String getAuthorPrincipalName(File file) {
        return null;
    }

    public String getTitleKey() {
        return null;
    }

}
