package edu.cornell.kfs.sys.businessobject;

import java.util.HashMap;
import java.util.Map;

public interface XmlFragmentable {
    
    public String getXmlPrefix();
    
    public boolean shouldMarshalAsFragment();
    
    public default Map<String, Object> getAdditionalJAXBProperties() {
        Map<String, Object> properties = new HashMap<>();
        return properties;
    }
}
