package edu.cornell.kfs.cemi.module.cg.batch.factory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.cemi.module.cg.batch.businessobject.CemiAwardLegacyNovelutionBo;

public class CemiAwardLegacyNovelutionBoFactory {
    private static final Logger LOG = LogManager.getLogger();
    
    public CemiAwardLegacyNovelutionBoFactory() {
    }
    
    public static CemiAwardLegacyNovelutionBo createEmptyCemiAwardLegacyNovelutionBo() {
        CemiAwardLegacyNovelutionBo emptyBo = new CemiAwardLegacyNovelutionBo();
        emptyBo.setProposalNumber(KFSConstants.EMPTY_STRING);
        emptyBo.setSpreadsheetKey(KFSConstants.EMPTY_STRING);
        emptyBo.setAwardSignedDate(KFSConstants.EMPTY_STRING);
        emptyBo.setCostShareTotalAmount(KFSConstants.EMPTY_STRING);
        emptyBo.setAnticipatedSponsorDirectCostAmount(KFSConstants.EMPTY_STRING);
        emptyBo.setAnticipatedFacilitiesAndAdministrationAmount(KFSConstants.EMPTY_STRING);
        emptyBo.setFederalAwardIdNumber(KFSConstants.EMPTY_STRING);
        emptyBo.setCfdaNumber(KFSConstants.EMPTY_STRING);
        return emptyBo;
    }
}
