package edu.cornell.kfs.fp.document;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.kuali.kfs.coreservice.framework.parameter.ParameterConstants.COMPONENT;
import org.kuali.kfs.coreservice.framework.parameter.ParameterConstants.NAMESPACE;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.fp.CuFPParameterConstants;
import edu.cornell.kfs.fp.businessobject.CorporateBilledCorporatePaidSourceAccountingLine;
import edu.cornell.kfs.fp.businessobject.CorporateBilledCorporatePaidTargetAccountingLine;
import edu.cornell.kfs.fp.businessobject.CorporateBilledCorporatePaidTransactionDetail;


@NAMESPACE(namespace = KFSConstants.CoreModuleNamespaces.FINANCIAL)
@COMPONENT(component = CuFPParameterConstants.CorporateBilledCorporatePaidDocument.COMPONENT_NAME_PARAMETER_NAME)
public class CorporateBilledCorporatePaidDocument extends CuProcurementCardDocument {
    private static final long serialVersionUID = 8032811224624474218L;
    
    @Override
    public String getFinancialDocumentTypeCode() {
        return CuFPConstants.CORPORATE_BILLED_CORPORATE_PAID_DOCUMENT_TYPE_CODE;
    }
    
    @Override
    public List getTargetAccountingLines() {
        List targetAccountingLines = new ArrayList();

        for (Iterator iter = transactionEntries.iterator(); iter.hasNext(); ) {
            CorporateBilledCorporatePaidTransactionDetail transactionEntry = (CorporateBilledCorporatePaidTransactionDetail) iter.next();
            for (Iterator iterator = transactionEntry.getTargetAccountingLines().iterator(); iterator.hasNext(); ) {
                CorporateBilledCorporatePaidTargetAccountingLine targetLine = (CorporateBilledCorporatePaidTargetAccountingLine) iterator.next();
                targetAccountingLines.add(targetLine);
            }
        }

        return targetAccountingLines;
    }
    
    @Override
    public List getSourceAccountingLines() {
        List sourceAccountingLines = new ArrayList();

        for (Iterator iter = transactionEntries.iterator(); iter.hasNext(); ) {
            CorporateBilledCorporatePaidTransactionDetail transactionEntry = (CorporateBilledCorporatePaidTransactionDetail) iter.next();
            for (Iterator iterator = transactionEntry.getSourceAccountingLines().iterator(); iterator.hasNext(); ) {
                CorporateBilledCorporatePaidSourceAccountingLine sourceLine = (CorporateBilledCorporatePaidSourceAccountingLine) iterator.next();
                sourceAccountingLines.add(sourceLine);
            }
        }

        return sourceAccountingLines;
    }
    
    
    
}
