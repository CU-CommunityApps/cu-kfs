package edu.cornell.kfs.fp.document;

import org.kuali.kfs.coreservice.framework.parameter.ParameterConstants.COMPONENT;
import org.kuali.kfs.coreservice.framework.parameter.ParameterConstants.NAMESPACE;
import org.kuali.kfs.fp.document.ProcurementCardDocument;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.fp.CuFPConstants;


@NAMESPACE(namespace = KFSConstants.CoreModuleNamespaces.FINANCIAL)
@COMPONENT(component = "CorporateBilledCorporatePaid")
public class CorporateBilledCorporatePaidDocument extends ProcurementCardDocument {
    private static final long serialVersionUID = 8032811224624474218L;
    protected static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CorporateBilledCorporatePaidDocument.class);
    
    @Override
    public String getFinancialDocumentTypeCode() {
        return CuFPConstants.CORPORATE_BILLED_CORPORATE_PAID_DOCUMENT_TYPE_CODE;
    }

}
