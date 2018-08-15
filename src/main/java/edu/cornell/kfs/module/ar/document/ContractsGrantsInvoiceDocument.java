package edu.cornell.kfs.module.ar.document;


public class ContractsGrantsInvoiceDocument extends org.kuali.kfs.module.ar.document.ContractsGrantsInvoiceDocument {
    protected static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ContractsGrantsInvoiceDocument.class);
    private static final long serialVersionUID = -4367230868222021386L;
    
    public ContractsGrantsInvoiceDocument() {
        super();
        LOG.info("called constructor");
    }
    
    @Override
    public String getDocumentTitle() {
        LOG.info("getDocumentTitle, entering");
        String documentTitle = super.getDocumentTitle();
        return documentTitle;
    }
    
    @Override
    public String getBillByChartOfAccountCode() {
        LOG.info("getBillByChartOfAccountCode, entering");
        return super.getBillByChartOfAccountCode();
    }
    

}
