package edu.cornell.kfs.fp.document;

import org.kuali.kfs.coreservice.framework.parameter.ParameterConstants.COMPONENT;
import org.kuali.kfs.coreservice.framework.parameter.ParameterConstants.NAMESPACE;
import org.kuali.kfs.fp.document.ProcurementCardDocument;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntry;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySequenceHelper;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySourceDetail;

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
    
    @Override
    public boolean generateGeneralLedgerPendingEntries(GeneralLedgerPendingEntrySourceDetail glpeSourceDetail, GeneralLedgerPendingEntrySequenceHelper sequenceHelper) {
        LOG.info("generateGeneralLedgerPendingEntries, glpeSourceDetail:" + glpeSourceDetail);
        LOG.info("generateGeneralLedgerPendingEntries, sequenceHelper:" + sequenceHelper);
        boolean results = super.generateGeneralLedgerPendingEntries(glpeSourceDetail, sequenceHelper);
        LOG.info("generateGeneralLedgerPendingEntries, results: " + results);
        return results;
    }
    
    @Override
    protected boolean processOffsetGeneralLedgerPendingEntry(GeneralLedgerPendingEntrySequenceHelper sequenceHelper, GeneralLedgerPendingEntrySourceDetail postable, 
            GeneralLedgerPendingEntry explicitEntry, GeneralLedgerPendingEntry offsetEntry) {
        LOG.info("processOffsetGeneralLedgerPendingEntry, postable: " + postable);
        LOG.info("processOffsetGeneralLedgerPendingEntry, explicitEntry: " + explicitEntry);
        LOG.info("processOffsetGeneralLedgerPendingEntry, offsetEntry: " + offsetEntry);
        boolean results = super.processOffsetGeneralLedgerPendingEntry(sequenceHelper, postable, explicitEntry, offsetEntry);
        return results;
        
    }

}
