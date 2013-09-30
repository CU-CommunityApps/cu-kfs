package edu.cornell.kfs.fp.document;

import org.kuali.kfs.coa.businessobject.BalanceType;
import org.kuali.kfs.fp.document.JournalVoucherDocument;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.AccountingLineParser;
import org.kuali.rice.coreservice.framework.parameter.ParameterConstants.COMPONENT;
import org.kuali.rice.coreservice.framework.parameter.ParameterConstants.NAMESPACE;

import edu.cornell.kfs.fp.businessobject.CuJournalVoucherAccountingLineParser;

@NAMESPACE(namespace = KFSConstants.CoreModuleNamespaces.FINANCIAL)
@COMPONENT(component = "JournalVoucher")
public class CuJournalVoucherDocument extends JournalVoucherDocument {

    private static final long serialVersionUID = 1L;

    public CuJournalVoucherDocument() {
        super();
        this.balanceType = new BalanceType();
    }


	@Override
    public AccountingLineParser getAccountingLineParser() {
		return new CuJournalVoucherAccountingLineParser(getBalanceTypeCode());
    }

}
