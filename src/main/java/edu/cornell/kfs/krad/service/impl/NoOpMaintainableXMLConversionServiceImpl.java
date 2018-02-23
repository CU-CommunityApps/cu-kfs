package edu.cornell.kfs.krad.service.impl;

import org.kuali.kfs.krad.service.MaintainableXMLConversionService;

public class NoOpMaintainableXMLConversionServiceImpl implements MaintainableXMLConversionService {

    /**
     * This implementation just returns the same XML that was passed in.
     *
     * @see org.kuali.kfs.krad.service.MaintainableXMLConversionService#transformMaintainableXML(java.lang.String)
     *
     * @param xml
     * @return
     */
    @Override
    public String transformMaintainableXML(String xml) {
        return xml;
    }
    
    /**
     * This implementation just returns the same XML that was passed in.
     *
     * @see org.kuali.kfs.krad.service.MaintainableXMLConversionService#transformMaintainableNoteXML(java.lang.String)
     */
    @Override
    public String transformMaintainableNoteXML(String xml) {
        return xml;
    }

}
