package edu.cornell.kfs.krad.service.impl;

import org.kuali.kfs.krad.service.MaintainableXMLConversionService;

public class CuMaintainableXMLConversionServiceImpl extends CynergyMaintainableXMLConversionServiceImpl implements MaintainableXMLConversionService {

    protected CynergyMaintenanceXMLConverter createXMLConverter() {
        return new CuMaintenanceXMLConverter(classPropertyRuleMaps, dateRuleMap);
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
