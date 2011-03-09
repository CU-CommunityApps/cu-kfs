package edu.cornell.kfs.module.ezra.util;

import java.util.HashMap;
import java.util.Map;

public class EzraUtils {

	private static Map<String, String> agencyTypeMap;
	private static Map<String, String> proposalAwardStatusMap;
	private static Map<String, String> grantDescriptionMap;
	private static Map<String, String> proposalPurposeMap;
	
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
	
	public static Map<String, String> getProposalAwardStatusMap() {
		if (proposalAwardStatusMap == null) {
			proposalAwardStatusMap = new HashMap<String, String>();
			proposalAwardStatusMap.put("AMAF", "AF");
			proposalAwardStatusMap.put("AMNAC", "AF");
			proposalAwardStatusMap.put("AMOH", "AH");
			proposalAwardStatusMap.put("AMPC", "AC");
			proposalAwardStatusMap.put("AMPFF", "AP");
			proposalAwardStatusMap.put("AMRO", "AP");
			proposalAwardStatusMap.put("AMSC", "AS");
			proposalAwardStatusMap.put("AAF", "FD");
			proposalAwardStatusMap.put("APFFF", "FT");
			proposalAwardStatusMap.put("ARO", "OS");
			proposalAwardStatusMap.put("AMURO", "AM");
			proposalAwardStatusMap.put("AAC", "AW");
			proposalAwardStatusMap.put("AC", "C");
			proposalAwardStatusMap.put("ACPTR", "AR");
			proposalAwardStatusMap.put("ACOSP", "AO");
			proposalAwardStatusMap.put("AIN", "AI");
			proposalAwardStatusMap.put("AIPS", "IP");
			proposalAwardStatusMap.put("ANA", "AN");
			proposalAwardStatusMap.put("ANF", "AF");
			proposalAwardStatusMap.put("AOH", "AH");
			proposalAwardStatusMap.put("APA", "AA");
			proposalAwardStatusMap.put("APC", "AP");
			proposalAwardStatusMap.put("ASAP", "A");
			proposalAwardStatusMap.put("AS", "AC");
			proposalAwardStatusMap.put("ATIP", "T");
			proposalAwardStatusMap.put("AURO", "AU");
		}
		return proposalAwardStatusMap;
	}
	
	public static Map<String, String> getGrantDescriptionMap() {
		if (grantDescriptionMap == null) {
			grantDescriptionMap = new HashMap<String, String>();
			grantDescriptionMap.put("C", "CON");
			grantDescriptionMap.put("P", "COP");
			grantDescriptionMap.put("G", "GRT");
			grantDescriptionMap.put("I", "IPA");
			grantDescriptionMap.put("L", "LOA");
			grantDescriptionMap.put("M", "MTA");
			grantDescriptionMap.put("N", "NDA");
			grantDescriptionMap.put("O", "OTH");
			grantDescriptionMap.put("H", "OFF");
			grantDescriptionMap.put("R", "RAD");
		}
		return grantDescriptionMap;
	}
	
	public static Map<String, String> getProposalPurposeMap() {
		if (proposalPurposeMap == null) {
			proposalPurposeMap = new HashMap<String, String>();
			proposalPurposeMap.put("A", "A");
			proposalPurposeMap.put("G", "B");
			proposalPurposeMap.put("E", "C");
			proposalPurposeMap.put("F", "F");
			proposalPurposeMap.put("N", "I");
			proposalPurposeMap.put("I", "D");
			proposalPurposeMap.put("L", "L");
			proposalPurposeMap.put("M", "O");
			proposalPurposeMap.put("R", "R");
			proposalPurposeMap.put("P", "P");
			proposalPurposeMap.put("S", "S");
			proposalPurposeMap.put("T", "T");
		}
		return proposalPurposeMap;
	}
	
}
