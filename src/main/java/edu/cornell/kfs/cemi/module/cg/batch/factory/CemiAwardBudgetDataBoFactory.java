package edu.cornell.kfs.cemi.module.cg.batch.factory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.cemi.module.cg.batch.businessobject.CemiAwardBudgetDataBo;

public class CemiAwardBudgetDataBoFactory {
    private static final Logger LOG = LogManager.getLogger();
    
    public CemiAwardBudgetDataBoFactory() {
    }
    
    public static CemiAwardBudgetDataBo createEmptyCemiAwardBudgetDataBo() {
        CemiAwardBudgetDataBo emptyBo = new CemiAwardBudgetDataBo();
        emptyBo.setProposalNumberUsedForDataRow(KFSConstants.EMPTY_STRING);
        emptyBo.setChartOfAccountsCode(KFSConstants.EMPTY_STRING);
        emptyBo.setAccountNumber(KFSConstants.EMPTY_STRING);
        emptyBo.setAwardLineDataRowId(KFSConstants.EMPTY_STRING);
        emptyBo.setSpecialConditionDataRowId(KFSConstants.EMPTY_STRING);
        
        emptyBo.setAwardBudgetDataRowId(KFSConstants.EMPTY_STRING);
        emptyBo.setDefaultBudgetStructure(KFSConstants.EMPTY_STRING);
        emptyBo.setDefaultBudgetType(KFSConstants.EMPTY_STRING);
        emptyBo.setDefaultBalancedAmendment(KFSConstants.EMPTY_STRING);
        return emptyBo;
    }
}
