package edu.cornell.kfs.module.ar.service.impl;

import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.integration.cg.ContractsAndGrantsBillingAward;
import org.kuali.kfs.integration.cg.ContractsAndGrantsBillingAwardAccount;
import org.kuali.kfs.krad.util.ErrorMessage;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.ar.businessobject.ContractsGrantsLetterOfCreditReviewDetail;
import org.kuali.kfs.module.ar.businessobject.InvoiceAccountDetail;
import org.kuali.kfs.module.ar.document.ContractsGrantsInvoiceDocument;
import org.kuali.kfs.module.ar.service.impl.ContractsGrantsInvoiceCreateDocumentServiceImpl;

import edu.cornell.kfs.module.ar.CuArParameterConstants;
import edu.cornell.kfs.module.ar.document.service.CuContractsGrantsInvoiceDocumentService;
import edu.cornell.kfs.module.ar.service.CuContractsGrantsInvoiceCreateDocumentService;

public class CuContractsGrantsInvoiceCreateDocumentServiceImpl extends ContractsGrantsInvoiceCreateDocumentServiceImpl implements CuContractsGrantsInvoiceCreateDocumentService {
    
    private static final Logger LOG = LogManager.getLogger();
    
    protected CuContractsGrantsInvoiceDocumentService cuContractsGrantsInvoiceDocumentService;
    
    /*
     * CUMod: KFSPTS-12866 
     */
    @Override
    public void populateDocumentDescription(ContractsGrantsInvoiceDocument cgInvoiceDocument) {
        if (ObjectUtils.isNotNull(cgInvoiceDocument) && ObjectUtils.isNotNull(cgInvoiceDocument.getInvoiceGeneralDetail())) {
            String proposalNumber = cgInvoiceDocument.getInvoiceGeneralDetail().getProposalNumber();
            if (StringUtils.isNotBlank(proposalNumber)) {
                String contractControlAccount = findContractControlAccountNumber(cgInvoiceDocument.getAccountDetails());
                String newTitle =  MessageFormat.format(findTitleFormatString(), proposalNumber, contractControlAccount);
                LOG.info("populateDocumentDescription, setting document description to " + newTitle);
                cgInvoiceDocument.getDocumentHeader().setDocumentDescription(newTitle);
            } else {
                LOG.error("populateDocumentDescription, unable to set the document description due to the proposal number being null");
            }
        } else {
            LOG.error("populateDocumentDescription, unable to set the document description due to the document or invoice general details being null");
        }
    }
    
    /*
     * CUMod: KFSPTS-12866, KFSPTS-14929
     */
    protected String findContractControlAccountNumber(List<InvoiceAccountDetail> details) {
        for (InvoiceAccountDetail detail : details) {
            Account contractControlAccount = getCuContractsGrantsInvoiceDocumentService().determineContractControlAccount(detail);
            if (ObjectUtils.isNotNull(contractControlAccount) 
                    && StringUtils.isNotBlank(contractControlAccount.getAccountNumber())) {
                return contractControlAccount.getAccountNumber();
            }
        }
        return StringUtils.EMPTY;
    }
    
    /*
     * CUMod: KFSPTS-12866
     */
    protected String findTitleFormatString() {
        return getConfigurationService().getPropertyValueAsString(CuArParameterConstants.CONTRACTS_GRANTS_INVOICE_DOCUMENT_TITLE_FORMAT);
    }

    @Override
    public ContractsGrantsInvoiceDocument createCGInvoiceDocumentByAwardInfo(ContractsAndGrantsBillingAward awd,
            List<ContractsAndGrantsBillingAwardAccount> accounts, String chartOfAccountsCode, String organizationCode,
            List<ErrorMessage> errorMessages, List<ContractsGrantsLetterOfCreditReviewDetail> accountDetails,
            String locCreationType) {
        ContractsGrantsInvoiceDocument cgInvoiceDocument = super.createCGInvoiceDocumentByAwardInfo(awd, accounts, chartOfAccountsCode, 
                organizationCode, errorMessages, accountDetails, locCreationType);
        //CUMod: KFSPTS-12866
        populateDocumentDescription(cgInvoiceDocument);
        return cgInvoiceDocument;
    }

    /*
     * CUMod: KFSPTS-14929
     */
    public CuContractsGrantsInvoiceDocumentService getCuContractsGrantsInvoiceDocumentService() {
        return cuContractsGrantsInvoiceDocumentService;
    }
    
    /*
     * CUMod: KFSPTS-14929
     */
    public void setCuContractsGrantsInvoiceDocumentService(
            CuContractsGrantsInvoiceDocumentService cuContractsGrantsInvoiceDocumentService) {
        this.cuContractsGrantsInvoiceDocumentService = cuContractsGrantsInvoiceDocumentService;
    }

}
