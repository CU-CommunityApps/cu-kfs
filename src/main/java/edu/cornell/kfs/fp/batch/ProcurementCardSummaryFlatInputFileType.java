package edu.cornell.kfs.fp.batch;

import java.io.File;
import java.util.ArrayList;

import org.kuali.kfs.sys.batch.FlatFileParserBase;
import org.kuali.kfs.sys.exception.ParseException;

import edu.cornell.kfs.fp.businessobject.ProcurementCardSummary;


public class ProcurementCardSummaryFlatInputFileType extends FlatFileParserBase {

    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ProcurementCardSummaryFlatInputFileType.class);
    protected ArrayList<String> errorMessages;
    protected int lineCount = 1;

    /**
     * @see org.kuali.kfs.sys.batch.FlatFileParserBase#getFileTypeIdentifer()
     */
    public String getFileTypeIdentifer() {

        return "procurementCardSummaryFlatInputFileType";
    }

    /**
     * @see org.kuali.kfs.sys.batch.FlatFileParserBase#parse(byte[])
     */
    public Object parse(byte[] fileByteContent) throws ParseException {
        ArrayList<ProcurementCardSummary> procurementCardSummaryEntries = new ArrayList<ProcurementCardSummary>();
        procurementCardSummaryEntries = (ArrayList<ProcurementCardSummary>) super.parse(fileByteContent);
        return procurementCardSummaryEntries;

    }

    /**
     * @see org.kuali.kfs.sys.batch.FlatFileParserBase#validate(java.lang.Object)
     */
    public boolean validate(Object parsedFileContents) {
        return false;
    }

    /**
     * @see org.kuali.kfs.sys.batch.FlatFileParserBase#process(java.lang.String,
     * java.lang.Object)
     */
    public void process(String fileName, Object parsedFileContents) {

    }

    /**
     * @see org.kuali.kfs.sys.batch.FlatFileParserBase#getAuthorPrincipalName(java.io.File)
     */
    public String getAuthorPrincipalName(File file) {
        return null;
    }

    /**
     * @see org.kuali.kfs.sys.batch.FlatFileParserBase#getTitleKey()
     */
    public String getTitleKey() {
        return null;
    }

}
