package edu.cornell.kfs.sys.businessobject;

public interface XmlFragmentable {
    
    public String getXMLPrefix();
    
    public boolean shouldMarshalAsFragment();
}
