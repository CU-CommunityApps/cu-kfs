package edu.cornell.kfs.sys.businessobject;

public interface XMLFragmentable {
    
    public String getXMLPrefix();
    
    public boolean shouldMarshalAsFragment();
}
