package edu.cornell.kfs.sys.batch;

import edu.cornell.kfs.sys.businessobject.KualiDeveloper;
import org.kuali.kfs.sys.batch.FlatFileParserBase;
import org.kuali.kfs.sys.exception.ParseException;

import java.io.File;
import java.util.ArrayList;

public class KualiDeveloperFlatInputFileType extends FlatFileParserBase {

    protected ArrayList<String> errorMessages;
    protected int lineCount = 1;

    /**
     * @see FlatFileParserBase#getFileTypeIdentifer()
     */
    public String getFileTypeIdentifer() {
        return "kualiDeveloperFlatInputFileType";
    }

    /**
     * @see FlatFileParserBase#parse(byte[])
     */
    public Object parse(byte[] fileByteContent) throws ParseException {
        ArrayList<KualiDeveloper> kualiDeveloperEntries = (ArrayList<KualiDeveloper>) super.parse(fileByteContent);
        return kualiDeveloperEntries;
    }

    /**
     * @see FlatFileParserBase#validate(Object)
     */
    public boolean validate(Object parsedFileContents) {
        return false;
    }

    /**
     * @see FlatFileParserBase#process(String,
     * Object)
     */
    public void process(String fileName, Object parsedFileContents) {
        System.out.println("HIT");
    }

    /**
     * @see FlatFileParserBase#getAuthorPrincipalName(File)
     */
    public String getAuthorPrincipalName(File file) {
        return null;
    }

    /**
     * @see FlatFileParserBase#getTitleKey()
     */
    public String getTitleKey() {
        return null;
    }

}
