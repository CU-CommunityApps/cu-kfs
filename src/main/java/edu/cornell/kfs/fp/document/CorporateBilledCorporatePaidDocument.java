package edu.cornell.kfs.fp.document;

import java.util.List;

import org.kuali.kfs.coreservice.framework.parameter.ParameterConstants.COMPONENT;
import org.kuali.kfs.coreservice.framework.parameter.ParameterConstants.NAMESPACE;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.fp.businessobject.CorporateBilledCorporatePaidTransactionDetail;


@NAMESPACE(namespace = KFSConstants.CoreModuleNamespaces.FINANCIAL)
@COMPONENT(component = "CorporateBilledCorporatePaid")
public class CorporateBilledCorporatePaidDocument extends CuProcurementCardDocument {
    private static final long serialVersionUID = 8032811224624474218L;
    
    private List<CorporateBilledCorporatePaidTransactionDetail> transactionEntries;
    
    @Override
    public String getFinancialDocumentTypeCode() {
        return CuFPConstants.CORPORATE_BILLED_CORPORATE_PAID_DOCUMENT_TYPE_CODE;
    }
    
    @Override
    public List<CorporateBilledCorporatePaidTransactionDetail> getTransactionEntries() {
        return transactionEntries;
    }
   
    @Override
    public void setTransactionEntries(List transactionEntries) {
        this.transactionEntries = transactionEntries;
    }
}
