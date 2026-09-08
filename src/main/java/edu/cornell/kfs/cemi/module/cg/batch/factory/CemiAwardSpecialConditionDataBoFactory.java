package edu.cornell.kfs.cemi.module.cg.batch.factory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.cemi.module.cg.batch.businessobject.CemiAwardSpecialConditionDataBo;

public class CemiAwardSpecialConditionDataBoFactory {
    private static final Logger LOG = LogManager.getLogger();
    
    public CemiAwardSpecialConditionDataBoFactory() {
    }
    
    public static CemiAwardSpecialConditionDataBo createEmptyCemiAwardSpecialConditionDataBo() {
        CemiAwardSpecialConditionDataBo emptyBo = new CemiAwardSpecialConditionDataBo();
        emptyBo.setProposalNumberUsedForDataRow(KFSConstants.EMPTY_STRING);
        emptyBo.setChartOfAccountsCode(KFSConstants.EMPTY_STRING);
        emptyBo.setAccountNumber(KFSConstants.EMPTY_STRING);
        emptyBo.setAwardLineDataRowId(KFSConstants.EMPTY_STRING);
        emptyBo.setSpecialConditionDataRowId(KFSConstants.EMPTY_STRING);
        emptyBo.setSpecialConditionDataDelete(KFSConstants.EMPTY_STRING);
        emptyBo.setSpecialCondition(KFSConstants.EMPTY_STRING);
        emptyBo.setSpecialConditionReferenceId(KFSConstants.EMPTY_STRING);
        emptyBo.setSpecialConditionType(KFSConstants.EMPTY_STRING);
        emptyBo.setSpecialConditionComment(KFSConstants.EMPTY_STRING);
        return emptyBo;
    }
    
}
