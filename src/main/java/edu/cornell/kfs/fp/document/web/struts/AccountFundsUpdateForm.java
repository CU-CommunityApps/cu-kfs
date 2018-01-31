package edu.cornell.kfs.fp.document.web.struts;

import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.fp.document.AccountFundsUpdateDocument;
import org.kuali.kfs.sys.web.struts.KualiAccountingDocumentFormBase;

public class AccountFundsUpdateForm extends KualiAccountingDocumentFormBase {
    public AccountFundsUpdateForm() {
        super();
    }

    @Override
    protected String getDefaultDocumentTypeName() {
        return CuFPConstants.AccountFundsUpdateDocumentConstants.ACCOUNT_FUNDS_UPDATE_DOCUMENT_TYPE_NAME;
    }

    public AccountFundsUpdateDocument getAccountFundsUpdateDocument() {
        return (AccountFundsUpdateDocument) getDocument();
    }
}