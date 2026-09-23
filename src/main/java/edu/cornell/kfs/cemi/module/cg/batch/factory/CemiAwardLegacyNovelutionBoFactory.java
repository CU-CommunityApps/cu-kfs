package edu.cornell.kfs.cemi.module.cg.batch.factory;

import edu.cornell.kfs.cemi.module.cg.batch.businessobject.CemiAwardLegacyNovelutionBo;
import edu.cornell.kfs.cemi.sys.CemiBaseConstants;

public class CemiAwardLegacyNovelutionBoFactory {
    
    public CemiAwardLegacyNovelutionBoFactory() {
    }
    
    public static CemiAwardLegacyNovelutionBo createEmptyCemiAwardLegacyNovelutionBo() {
        CemiAwardLegacyNovelutionBo emptyBo = new CemiAwardLegacyNovelutionBo();
        emptyBo.setProposalNumber(CemiBaseConstants.EMPTY_STRING);
        emptyBo.setSpreadsheetKey(CemiBaseConstants.EMPTY_STRING);
        emptyBo.setAwardSignedDate(CemiBaseConstants.EMPTY_STRING);
        emptyBo.setCostShareTotalAmount(CemiBaseConstants.EMPTY_STRING);
        emptyBo.setAnticipatedSponsorDirectCostAmount(CemiBaseConstants.EMPTY_STRING);
        emptyBo.setAnticipatedFacilitiesAndAdministrationAmount(CemiBaseConstants.EMPTY_STRING);
        emptyBo.setFederalAwardIdNumber(CemiBaseConstants.EMPTY_STRING);
        emptyBo.setCfdaNumber(CemiBaseConstants.EMPTY_STRING);
        return emptyBo;
    }
}
