package edu.cornell.kfs.rass.batch.xml.fixture;

import java.util.Date;

import edu.cornell.kfs.rass.batch.xml.RassXmlDocumentWrapper;

public enum RassXmlDocumentWrapperFixture {
        RASS_EXAMPLE("2019-03-15T22:15:07.273");
    
    public final Date extractDate;
    
    private RassXmlDocumentWrapperFixture(String extractDateString) {
        extractDate = new Date(extractDateString);
    }
    
    public RassXmlDocumentWrapper toRassXmlDocumentWrapper() {
        RassXmlDocumentWrapper wrapper = new RassXmlDocumentWrapper();
        wrapper.setExtractDate(extractDate);
        return wrapper;
    }

}
