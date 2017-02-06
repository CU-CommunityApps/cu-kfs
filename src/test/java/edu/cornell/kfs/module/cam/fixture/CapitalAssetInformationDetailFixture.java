package edu.cornell.kfs.module.cam.fixture;

import org.kuali.kfs.fp.businessobject.CapitalAssetInformationDetail;

import edu.cornell.kfs.fp.businessobject.CapitalAssetInformationDetailExtendedAttribute;

public enum CapitalAssetInformationDetailFixture {
	
	ONE();
	
    private String documentNumber;
    private Integer capitalAssetLineNumber;
    private Integer itemLineNumber;
    private String campusCode;
    private String buildingCode;
    private String buildingRoomNumber;
    private String buildingSubRoomNumber;
    private String capitalAssetTagNumber;
    private String capitalAssetSerialNumber;
    private CapitalAssetInformationDetailExtendedAttribute capDetailExt;

	
	private CapitalAssetInformationDetailFixture() {
		
	}

	public CapitalAssetInformationDetail createCapitalAssetInformationDetail() {
		CapitalAssetInformationDetail capitalAssetInformationDetail = new CapitalAssetInformationDetail();
		
		return capitalAssetInformationDetail;
	}
	
}
