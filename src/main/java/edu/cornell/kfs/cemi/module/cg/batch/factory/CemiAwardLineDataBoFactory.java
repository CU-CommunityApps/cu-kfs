package edu.cornell.kfs.cemi.module.cg.batch.factory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.cemi.module.cg.batch.businessobject.CemiAwardLineDataBo;

public class CemiAwardLineDataBoFactory {
    private static final Logger LOG = LogManager.getLogger();
    
    public CemiAwardLineDataBoFactory() {
    }
    
    public static CemiAwardLineDataBo createEmptyCemiAwardLineDataBo() {
        CemiAwardLineDataBo emptyCemiAwardLineDataBo = new CemiAwardLineDataBo();
        emptyCemiAwardLineDataBo.setProposalNumberUsedForDataRow(KFSConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setChartOfAccountsCode(KFSConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setAccountNumber(KFSConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setAwardLineDataRowId(KFSConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setReceivableContractLine(KFSConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setReceivableContractLineReferenceId(KFSConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setLineNumber(KFSConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setIntercompanyAffiliate(KFSConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setRevenueCategory(KFSConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setAwardLineDataAwardLifecycleStatus(KFSConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setLineType(KFSConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setSpendRestriction(KFSConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setLineItemDescriptionOverride(KFSConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setDeferredRevenue(KFSConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setLineStatus(KFSConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setAwardLineDocumentStatus(KFSConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setPrimaryGrant(KFSConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setLineCfdaNumber(KFSConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setGrantId(KFSConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setLineAmount(KFSConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setRateAgreement(KFSConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setCostRateType(KFSConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setException(KFSConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setRevenueAllocationProfile(KFSConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setDelete(KFSConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setBasisLimit(KFSConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setBasisLimitId(KFSConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setBasisLimitName(KFSConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setBasisLimitAmount(KFSConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setAwardLineStartDate(KFSConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setAwardLineEndDate(KFSConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setAwardLineDescription(KFSConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setLineInvoiceMemoOverride(KFSConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setAwardLineDataCostCenter(KFSConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setAwardLineDataFund(KFSConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setAwardLineDataProgram(KFSConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setAwardLineSalaryCap(KFSConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setAwardLineSalaryCapOverride(KFSConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setSubrecipient(KFSConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setLineFederalAwardIdNumber(KFSConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setLineBillingNotes(KFSConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setRevenueRecognitionLineNotes(KFSConstants.EMPTY_STRING);
       return emptyCemiAwardLineDataBo;
    }
    
}
