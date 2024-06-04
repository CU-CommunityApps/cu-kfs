/*
 * Copyright 2009 The Kuali Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rsmart.kuali.kfs.cr.document.service.impl;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.coa.businessobject.AccountingPeriod;
import org.kuali.kfs.coa.businessobject.OffsetDefinition;
import org.kuali.kfs.coa.service.AccountingPeriodService;
import org.kuali.kfs.coa.service.OffsetDefinitionService;
import org.kuali.kfs.pdp.businessobject.GlPendingTransaction;
import org.kuali.kfs.pdp.businessobject.PaymentAccountDetail;
import org.kuali.kfs.pdp.businessobject.PaymentDetail;
import org.kuali.kfs.pdp.businessobject.PaymentGroup;
import org.kuali.kfs.pdp.service.PendingTransactionService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.Bank;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySequenceHelper;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.FlexibleOffsetAccountService;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.core.api.util.type.KualiInteger;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.ObjectUtils;

import com.rsmart.kuali.kfs.cr.CRConstants;
import com.rsmart.kuali.kfs.cr.batch.CheckReconciliationImportStep;
import com.rsmart.kuali.kfs.cr.businessobject.CheckReconciliation;
import com.rsmart.kuali.kfs.cr.dataaccess.CheckReconciliationDao;
import com.rsmart.kuali.kfs.cr.document.service.GlTransactionService;

/**
 * GlTransactionServiceImpl
 * 
 * @author Derek Helbert
 */
public class GlTransactionServiceImpl implements GlTransactionService {

    private AccountingPeriodService accountingPeriodService;
    
    private BusinessObjectService businessObjectService;
    
    private PendingTransactionService pendingTransactionService;

    private ParameterService parameterService;
    
    private DateTimeService dateTimeService;
    
    private CheckReconciliationDao checkReconciliationDao;
    
    private static String FDOC_TYP_CD_CANCEL_CHECK = "CHKC";
    
    private static String FDOC_TYP_CD_STOP_CHECK = "CHKS";
    
    private static String FDOC_TYP_CD_STALE_CHECK = "CHKL";
    
    /**
     * 
     * @see com.rsmart.kuali.kfs.cr.document.service.GlTransactionService#getCanceledChecks()
     */
    public Collection<Integer> getCanceledChecks() {
        return checkReconciliationDao.getCanceledChecks();
    }
    
    /**
     * 
     * @see com.rsmart.kuali.kfs.cr.document.service.GlTransactionService#getNewCheckReconciliations(java.util.Collection)
     */
    public Collection<CheckReconciliation> getNewCheckReconciliations(Collection<Bank> banks) {
        return checkReconciliationDao.getNewCheckReconciliations(banks);
    }
    
    /**
     * 
     * @see com.rsmart.kuali.kfs.cr.document.service.GlTransactionService#getAllPaymentGroupForSearchCriteria(java.lang.String, java.util.Collection)
     */
    public List<PaymentGroup> getAllPaymentGroupForSearchCriteria(KualiInteger disbNbr, Collection bankCodes) {
        return checkReconciliationDao.getAllPaymentGroupForSearchCriteria(disbNbr, bankCodes);
    }
    
    /**
     * 
     * @see com.rsmart.kuali.kfs.cr.document.service.GlTransactionService#generateGlPendingTransactionStale(org.kuali.kfs.pdp.businessobject.PaymentGroup)
     */
    public void generateGlPendingTransactionStale(PaymentGroup paymentGroup) {
        generateGlPendingTransaction(paymentGroup,FDOC_TYP_CD_STALE_CHECK,true);
    }
    
    /**
     * 
     * @see com.rsmart.kuali.kfs.cr.document.service.GlTransactionService#generateGlPendingTransactionCancel(org.kuali.kfs.pdp.businessobject.PaymentGroup)
     */
    public void generateGlPendingTransactionCancel(PaymentGroup paymentGroup) {
        generateGlPendingTransaction(paymentGroup,FDOC_TYP_CD_CANCEL_CHECK,false);
    }
    
    /**
     * 
     * @see com.rsmart.kuali.kfs.cr.document.service.GlTransactionService#generateGlPendingTransactionStop(org.kuali.kfs.pdp.businessobject.PaymentGroup)
     */
    public void generateGlPendingTransactionStop(PaymentGroup paymentGroup) {
        generateGlPendingTransaction(paymentGroup,FDOC_TYP_CD_STOP_CHECK,false);
    }
    
    /**
     * Generate GlPendingTransaction
     * 
     * @param paymentGroup
     * @param financialDocumentTypeCode
     * @param stale
     */
    private void generateGlPendingTransaction(PaymentGroup paymentGroup, String financialDocumentTypeCode, boolean stale) {
        List<PaymentAccountDetail> accountListings = new ArrayList<PaymentAccountDetail>();
        
        for (PaymentDetail paymentDetail : paymentGroup.getPaymentDetails()) {
            accountListings.addAll(paymentDetail.getAccountDetail());
        }

        GeneralLedgerPendingEntrySequenceHelper sequenceHelper = new GeneralLedgerPendingEntrySequenceHelper();
        
        for (PaymentAccountDetail paymentAccountDetail : accountListings) {
            GlPendingTransaction glPendingTransaction = new GlPendingTransaction();
            glPendingTransaction.setSequenceNbr(new KualiInteger(sequenceHelper.getSequenceCounter()));
            glPendingTransaction.setFdocRefTypCd(paymentAccountDetail.getPaymentDetail().getFinancialDocumentTypeCode());
            glPendingTransaction.setFsRefOriginCd(paymentAccountDetail.getPaymentDetail().getFinancialSystemOriginCode());
            glPendingTransaction.setFinancialBalanceTypeCode(KFSConstants.BALANCE_TYPE_ACTUAL);

            Date transactionTimestamp = new Date(dateTimeService.getCurrentDate().getTime());
            glPendingTransaction.setTransactionDt(transactionTimestamp);
            
            AccountingPeriod fiscalPeriod = accountingPeriodService.getByDate(new java.sql.Date(transactionTimestamp.getTime()));
            glPendingTransaction.setUniversityFiscalYear(fiscalPeriod.getUniversityFiscalYear());
            glPendingTransaction.setUnivFiscalPrdCd(fiscalPeriod.getUniversityFiscalPeriodCode());
            glPendingTransaction.setSubAccountNumber(paymentAccountDetail.getSubAccountNbr());
            glPendingTransaction.setChartOfAccountsCode(paymentAccountDetail.getFinChartCode());
            glPendingTransaction.setFdocNbr(paymentGroup.getDisbursementNbr().toString());
            
            // Set doc type and origin code
            glPendingTransaction.setFinancialDocumentTypeCode(financialDocumentTypeCode);
            glPendingTransaction.setFsOriginCd(CRConstants.CR_FDOC_ORIGIN_CODE);
            
            String clAcct  = parameterService.getParameterValueAsString(CheckReconciliationImportStep.class,CRConstants.CLEARING_ACCOUNT);
            String obCode  = parameterService.getParameterValueAsString(CheckReconciliationImportStep.class,CRConstants.CLEARING_OBJECT_CODE);
            String coaCode = parameterService.getParameterValueAsString(CheckReconciliationImportStep.class,CRConstants.CLEARING_COA);
            
            // Use clearing parameters if stale
            String accountNbr    = stale ? clAcct  : paymentAccountDetail.getAccountNbr();
            String finObjectCode = stale ? obCode  : paymentAccountDetail.getFinObjectCode();
            String finCoaCd      = stale ? coaCode : paymentAccountDetail.getFinChartCode();
            
            Boolean relieveLiabilities = paymentGroup.getBatch().getCustomerProfile().getRelieveLiabilities();
            if ((relieveLiabilities != null) && (relieveLiabilities.booleanValue()) && paymentAccountDetail.getPaymentDetail().getFinancialDocumentTypeCode() != null) {
                OffsetDefinition offsetDefinition = SpringContext.getBean(OffsetDefinitionService.class).getActiveByPrimaryId(glPendingTransaction.getUniversityFiscalYear(), glPendingTransaction.getChartOfAccountsCode(), paymentAccountDetail.getPaymentDetail().getFinancialDocumentTypeCode(), glPendingTransaction.getFinancialBalanceTypeCode()).orElse(null);
                
                glPendingTransaction.setAccountNumber(accountNbr);
                glPendingTransaction.setChartOfAccountsCode(finCoaCd);
                glPendingTransaction.setFinancialObjectCode(ObjectUtils.isNotNull(offsetDefinition) ? offsetDefinition.getFinancialObjectCode() : finObjectCode);
                glPendingTransaction.setFinancialSubObjectCode(KFSConstants.getDashFinancialSubObjectCode());
            }
            else {
                glPendingTransaction.setAccountNumber(accountNbr);
                glPendingTransaction.setChartOfAccountsCode(finCoaCd);
                glPendingTransaction.setFinancialObjectCode(finObjectCode);
                glPendingTransaction.setFinancialSubObjectCode(paymentAccountDetail.getFinSubObjectCode());
            }

            glPendingTransaction.setProjectCd(paymentAccountDetail.getProjectCode());

            if (paymentAccountDetail.getAccountNetAmount().bigDecimalValue().signum() >= 0) {
                glPendingTransaction.setDebitCrdtCd(KFSConstants.GL_CREDIT_CODE);
            }
            else {
                glPendingTransaction.setDebitCrdtCd(KFSConstants.GL_DEBIT_CODE);
            }
            glPendingTransaction.setAmount(paymentAccountDetail.getAccountNetAmount().abs());

            String trnDesc;

            String payeeName = paymentGroup.getPayeeName();
            trnDesc = payeeName.length() > 40 ? payeeName.substring(0, 40) : StringUtils.rightPad(payeeName, 40);

            String poNbr = paymentAccountDetail.getPaymentDetail().getPurchaseOrderNbr();
            if (StringUtils.isNotBlank(poNbr)) {
               trnDesc += " " + (poNbr.length() > 9 ? poNbr.substring(0, 9) : StringUtils.rightPad(poNbr, 9));
            }

            String invoiceNbr = paymentAccountDetail.getPaymentDetail().getInvoiceNbr();
            if (StringUtils.isNotBlank(invoiceNbr)) {
                trnDesc += " " + (invoiceNbr.length() > 14 ? invoiceNbr.substring(0, 14) : StringUtils.rightPad(invoiceNbr, 14));
            }

            if (trnDesc.length() > 40) {
                trnDesc = trnDesc.substring(0, 40);
            }

            glPendingTransaction.setDescription(trnDesc);

            glPendingTransaction.setOrgDocNbr(paymentAccountDetail.getPaymentDetail().getOrganizationDocNbr());
            glPendingTransaction.setOrgReferenceId(paymentAccountDetail.getOrgReferenceId());
            glPendingTransaction.setFdocRefNbr(paymentAccountDetail.getPaymentDetail().getCustPaymentDocNbr());

            // update the offset account if necessary
            SpringContext.getBean(FlexibleOffsetAccountService.class).updateOffset(glPendingTransaction);

            this.businessObjectService.save(glPendingTransaction);
            
            sequenceHelper.increment();
        }
        
    }
    
    /**
     * Get PendingTransactionService
     * 
     * @return PendingTransactionService
     */
    public PendingTransactionService getPendingTransactionService() {
        return pendingTransactionService;
    }

    /**
     * Set PendingTransactionService
     * 
     * @param pendingTransactionService
     */
    public void setPendingTransactionService(PendingTransactionService pendingTransactionService) {
        this.pendingTransactionService = pendingTransactionService;
    }

    /**
     * Get BusinessObjectService
     * 
     * @return BusinessObjectService
     */
    public BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    /**
     * Set BusinessObjectService
     * 
     * @param businessObjectService
     */
    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    /**
     * Get AccountingPeriodService
     * 
     * @return AccountingPeriodService
     */
    public AccountingPeriodService getAccountingPeriodService() {
        return accountingPeriodService;
    }

    /**
     * Set AccountingPeriodService
     * 
     * @param accountingPeriodService
     */
    public void setAccountingPeriodService(AccountingPeriodService accountingPeriodService) {
        this.accountingPeriodService = accountingPeriodService;
    }

    /**
     * Get ParameterService
     * 
     * @return ParameterService
     */
    public ParameterService getParameterService() {
        return parameterService;
    }

    /**
     * Set ParameterService
     * 
     * @param parameterService
     */
    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public DateTimeService getDateTimeService() {
        return dateTimeService;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    /**
     * Get CheckReconciliationDao
     * 
     * @return
     */
    public CheckReconciliationDao getCheckReconciliationDao() {
        return checkReconciliationDao;
    }

    /**
     * Set CheckReconciliationDao
     * 
     * @param checkReconciliationDao
     */
    public void setCheckReconciliationDao(CheckReconciliationDao checkReconciliationDao) {
        this.checkReconciliationDao = checkReconciliationDao;
    }



    
}
