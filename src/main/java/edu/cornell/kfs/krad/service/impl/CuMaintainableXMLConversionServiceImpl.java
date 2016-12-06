package edu.cornell.kfs.krad.service.impl;

import edu.cornell.cynergy.krad.service.impl.CynergyMaintainableXMLConversionServiceImpl;
import edu.cornell.cynergy.krad.service.impl.CynergyMaintenanceXMLConverter;
import org.kuali.kfs.krad.service.MaintainableXMLConversionService;

import java.util.List;

public class CuMaintainableXMLConversionServiceImpl extends CynergyMaintainableXMLConversionServiceImpl implements MaintainableXMLConversionService {

    public List<String> getConversionRuleFiles() {
        return conversionRuleFiles;
    }

    public void setConversionRuleFiles(List<String> conversionRuleFiles) {
        this.conversionRuleFiles = conversionRuleFiles;
    }

    protected List<String> conversionRuleFiles;

    protected CynergyMaintenanceXMLConverter createXMLConverter() {
        return new CuMaintenanceXMLConverter(classPropertyRuleMaps, dateRuleMap);
    }

}
