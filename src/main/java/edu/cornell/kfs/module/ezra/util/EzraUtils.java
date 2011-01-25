package edu.cornell.kfs.module.ezra.util;

import java.util.HashMap;
import java.util.Map;

public class EzraUtils {

	private static Map<String, String> agencyTypeMap;
	
	public static Map<String, String> getAgencyTypeMap() {
		if (agencyTypeMap == null) {
			agencyTypeMap = new HashMap<String, String>();
			agencyTypeMap.put("1", "F");
			agencyTypeMap.put("2", "S");
			agencyTypeMap.put("3", "C");
			agencyTypeMap.put("4", "O");
			agencyTypeMap.put("5", "N");
			agencyTypeMap.put("6", "I");
			agencyTypeMap.put("7", "L");
			agencyTypeMap.put("8", "G");
			agencyTypeMap.put("9", "U");
			agencyTypeMap.put("10", "W");
		} 
		return agencyTypeMap;
	}
	
}
