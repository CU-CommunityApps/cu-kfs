package edu.cornell.kfs.vnd.batch.service;

import java.util.Collection;

import org.kuali.kfs.vnd.businessobject.CommodityCode;

public interface CommodityCodeUpdateService {

	/**
	 * 
	 */
//    public void loadCommodityCodeFile(String fileName);
    
    public boolean loadCommodityCodeFile(String fileName);
  
    public byte[] getFileContent(String fileName);
    
    public Collection<CommodityCode> parseCommodityCodeList(byte[] fileContents);
}
