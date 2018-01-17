package edu.cornell.kfs.fp.document.web.struts;

import edu.cornell.kfs.fp.document.AccountFundsUpdateDocument;
import org.kuali.kfs.sys.web.struts.KualiAccountingDocumentFormBase;

public class AccountFundsUpdateForm extends KualiAccountingDocumentFormBase {
    public AccountFundsUpdateForm() {
        super();
    }

    @Override
    protected String getDefaultDocumentTypeName() {
        return "AFU";
    }

    public AccountFundsUpdateDocument getAccountFundsUpdateDocument() {
        return (AccountFundsUpdateDocument) getDocument();
    }
}