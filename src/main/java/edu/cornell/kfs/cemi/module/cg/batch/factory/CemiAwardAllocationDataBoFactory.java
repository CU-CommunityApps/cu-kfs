package edu.cornell.kfs.cemi.module.cg.batch.factory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.cemi.module.cg.batch.businessobject.CemiAwardAllocationDataBo;

public class CemiAwardAllocationDataBoFactory {
    private static final Logger LOG = LogManager.getLogger();
    
    public CemiAwardAllocationDataBoFactory() {
    }
    
    public static CemiAwardAllocationDataBo createEmptyCemiAwardAllocationDataBo() {
        CemiAwardAllocationDataBo emptyBo = new CemiAwardAllocationDataBo();
        emptyBo.setProposalNumberUsedForDataRow(KFSConstants.EMPTY_STRING);
        emptyBo.setChartOfAccountsCode(KFSConstants.EMPTY_STRING);
        emptyBo.setAccountNumber(KFSConstants.EMPTY_STRING);
        emptyBo.setAwardLineDataRowId(KFSConstants.EMPTY_STRING);
        emptyBo.setSpecialConditionDataRowId(KFSConstants.EMPTY_STRING);
        emptyBo.setBudgetDataRowId(KFSConstants.EMPTY_STRING);
        
        emptyBo.setNsfCodeAllocationDataRowId(KFSConstants.EMPTY_STRING);
        emptyBo.setNsfCodeAllocationDataDelete(KFSConstants.EMPTY_STRING);
        emptyBo.setNsfCodeAllocation(KFSConstants.EMPTY_STRING);
        emptyBo.setNsfCodeAllocationId(KFSConstants.EMPTY_STRING);
        emptyBo.setNsfCodeAllocationPercentage(KFSConstants.EMPTY_STRING);
        emptyBo.setNsfCode(KFSConstants.EMPTY_STRING);
        return emptyBo;
    }
    
}
