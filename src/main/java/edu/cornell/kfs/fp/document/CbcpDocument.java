package edu.cornell.kfs.fp.document;

import org.kuali.kfs.fp.document.ProcurementCardDocument;

import edu.cornell.kfs.fp.CuFPConstants;

public class CbcpDocument extends ProcurementCardDocument {
    private static final long serialVersionUID = 8032811224624474218L;
    protected static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CbcpDocument.class);
    
    @Override
    public String getFinancialDocumentTypeCode() {
        return CuFPConstants.CBCP_DOCUMENT_TYPE_CODE;
    }

}
