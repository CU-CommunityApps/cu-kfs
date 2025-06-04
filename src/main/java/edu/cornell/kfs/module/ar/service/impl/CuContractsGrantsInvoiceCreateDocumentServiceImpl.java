package edu.cornell.kfs.module.ar.service.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.module.cg.businessobject.Award;
import org.kuali.kfs.module.cg.businessobject.AwardAccount;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.ChartOrgHolder;
import org.kuali.kfs.krad.util.ErrorMessage;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.ar.ArConstants;
import org.kuali.kfs.module.ar.ArConstants.ContractsAndGrantsInvoiceDocumentCreationProcessType;
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
    public void populateDocumentDescription(final ContractsGrantsInvoiceDocument cgInvoiceDocument) {
        if (ObjectUtils.isNotNull(cgInvoiceDocument) && ObjectUtils.isNotNull(cgInvoiceDocument.getInvoiceGeneralDetail())) {
            final String proposalNumber = cgInvoiceDocument.getInvoiceGeneralDetail().getProposalNumber();
            if (StringUtils.isNotBlank(proposalNumber)) {
                final String contractControlAccount = findContractControlAccountNumber(cgInvoiceDocument.getAccountDetails());
                final String newTitle =  MessageFormat.format(findTitleFormatString(), proposalNumber, contractControlAccount);
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
    protected String findContractControlAccountNumber(final List<InvoiceAccountDetail> details) {
        for (InvoiceAccountDetail detail : details) {
            final Account contractControlAccount = getCuContractsGrantsInvoiceDocumentService().determineContractControlAccount(detail);
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
    public ContractsGrantsInvoiceDocument createCGInvoiceDocumentByAwardInfo(
            final Award awd,
            final List<AwardAccount> accounts, final String chartOfAccountsCode, final String organizationCode,
            final List<ErrorMessage> errorMessages, final List<ContractsGrantsLetterOfCreditReviewDetail> accountDetails,
            final String locCreationType) {
        final ContractsGrantsInvoiceDocument cgInvoiceDocument = super.createCGInvoiceDocumentByAwardInfo(awd, accounts, chartOfAccountsCode, 
                organizationCode, errorMessages, accountDetails, locCreationType);
        //CUMod: KFSPTS-12866
        populateDocumentDescription(cgInvoiceDocument);
        return cgInvoiceDocument;
    }
    

    public ContractsGrantsInvoiceDocument createCINVForReport(final Award awd) {
        final List<ErrorMessage> errorMessages = new ArrayList<>();
        return generateContractsAndGrantsInvoiceDocumentForReport(awd, awd.getActiveAwardAccounts(), errorMessages,
                ArConstants.ContractsAndGrantsInvoiceDocumentCreationProcessType.MANUAL, null, null);

    }

    protected ContractsGrantsInvoiceDocument generateContractsAndGrantsInvoiceDocumentForReport(final Award awd,
            final List<AwardAccount> validAwardAccounts, final List<ErrorMessage> errorMessages,
            final ContractsAndGrantsInvoiceDocumentCreationProcessType creationProcessType,
            final List<ContractsGrantsLetterOfCreditReviewDetail> accountDetails, final String locCreationType) {
        final ChartOrgHolder chartOrgHolder = financialSystemUserService.getPrimaryOrganization(
                awd.getAwardPrimaryFundManager().getFundManager().getPrincipalId(),
                KFSConstants.OptionalModuleNamespaces.ACCOUNTS_RECEIVABLE);

        awd.setCreationProcessType(creationProcessType);

        final ContractsGrantsInvoiceDocument cgInvoiceDocument = createCGInvoiceDocumentByAwardInfo(awd,
                validAwardAccounts, chartOrgHolder.getChartOfAccountsCode(), chartOrgHolder.getOrganizationCode(),
                errorMessages, accountDetails, locCreationType);
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
            final CuContractsGrantsInvoiceDocumentService cuContractsGrantsInvoiceDocumentService) {
        this.cuContractsGrantsInvoiceDocumentService = cuContractsGrantsInvoiceDocumentService;
    }

}
