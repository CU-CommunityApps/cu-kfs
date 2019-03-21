package edu.cornell.kfs.rass.batch.xml.fixture;

import java.util.Date;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import edu.cornell.kfs.rass.batch.xml.RassXmlDocumentWrapper;
import edu.cornell.kfs.sys.CUKFSConstants;

public enum RassXmlDocumentWrapperFixture {
        RASS_EXAMPLE("2019-03-15T22:15:07.273");
    
    public final Date extractDate;
    
    private RassXmlDocumentWrapperFixture(String extractDateString) {
        DateTimeFormatter dateformatter = DateTimeFormat.forPattern(CUKFSConstants.DATE_FOMRAT_yyyy_MM_dd_T_HH_mm_ss_SSS);
        extractDate = dateformatter.parseDateTime(extractDateString).toDate();
    }
    
    public RassXmlDocumentWrapper toRassXmlDocumentWrapper() {
        RassXmlDocumentWrapper wrapper = new RassXmlDocumentWrapper();
        wrapper.setExtractDate(extractDate);
        return wrapper;
    }

}
