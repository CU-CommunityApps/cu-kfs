package edu.cornell.kfs.fp.document;

import org.kuali.kfs.coreservice.framework.parameter.ParameterConstants.COMPONENT;
import org.kuali.kfs.coreservice.framework.parameter.ParameterConstants.NAMESPACE;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.fp.CuFPParameterConstants;

@NAMESPACE(namespace = KFSConstants.CoreModuleNamespaces.FINANCIAL)
@COMPONENT(component = CuFPParameterConstants.CorporateBilledCorporatePaidDocument.CBCP_COMPONENT_NAME)
public class CorporateBilledCorporatePaidDocument extends CuProcurementCardDocument {
    private static final long serialVersionUID = 8032811224624474218L;
    
    @Override
    public String getFinancialDocumentTypeCode() {
        return CuFPConstants.CORPORATE_BILLED_CORPORATE_PAID_DOCUMENT_TYPE_CODE;
    }
    
}
