package edu.cornell.kfs.module.ar.document;

import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.module.ar.businessobject.InvoiceAccountDetail;
import org.kuali.kfs.module.ar.document.ContractsGrantsInvoiceDocument;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.api.config.property.ConfigurationService;

import edu.cornell.kfs.fp.document.authorization.Logger;
import edu.cornell.kfs.module.ar.CuArParameterConstants;

public class CuContractsGrantsInvoiceDocument extends ContractsGrantsInvoiceDocument {
    private static final long serialVersionUID = 6257106079211623394L;
    private static final Logger LOG = LogManager.getLogger(CuContractsGrantsInvoiceDocument.class);
    
    protected transient ConfigurationService configurationService;

    @Override
    public String getDocumentTitle() {
        String documentTitle = buildDocumentTitle(super.getDocumentTitle());
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDocumentTitle, returning document title : " + documentTitle);
        }
        return documentTitle;
    }

    public String buildDocumentTitle(String originalTitle) {
        String proposalNumber = getInvoiceGeneralDetail().getProposalNumber();
        if (StringUtils.isNotBlank(proposalNumber)) {
            String contractControlAccount = findContractControlAccountNumber(getAccountDetails());
            return MessageFormat.format(findTitleFormatString(), proposalNumber, contractControlAccount);
        } else {
            LOG.error("buildDocumentTitle, unable to do get a proposal number for CINV document " + getDocumentNumber());
            return originalTitle;
        }

    }
    
    protected String findContractControlAccountNumber(List<InvoiceAccountDetail> details) {
        for (InvoiceAccountDetail detail : details) {
            if (StringUtils.isNotBlank(detail.getContractControlAccountNumber())) {
                return detail.getContractControlAccountNumber();
            }
        }
        LOG.error("findContractControlAccountNumber, could not find contract control account number");
        return StringUtils.EMPTY;
    }
    
    protected String findTitleFormatString() {
        return getConfigurationService().getPropertyValueAsString(CuArParameterConstants.CONTRACTS_GRANTS_INVOICE_DOCUMENT_TITLE_FORMAT);
    }

    public ConfigurationService getConfigurationService() {
        if (configurationService == null) {
            configurationService = SpringContext.getBean(ConfigurationService.class);
        }
        return configurationService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

}
