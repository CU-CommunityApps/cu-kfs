package edu.cornell.kfs.fp.document.authorization;

import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.fp.document.BudgetAdjustmentDocument;
import org.kuali.kfs.fp.document.authorization.BudgetAdjustmentDocumentPresentationController;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.sys.KFSConstants;

public class CuBudgetAdjustmentDocumentPresentationController extends BudgetAdjustmentDocumentPresentationController {
    @Override
    public Set<String> getDocumentActions(Document document) {
        final Set<String> documentActions = super.getDocumentActions(document);

        final BudgetAdjustmentDocument budgetAdjustmentDocument = (BudgetAdjustmentDocument) document;
        final String docInError = budgetAdjustmentDocument.getDocumentHeader().getFinancialDocumentInErrorNumber();
        
        if (StringUtils.isNotBlank(docInError)) {
            final Boolean allowBlanketApproveNoRequest = getParameterService().getParameterValueAsBoolean(
                    KFSConstants.CoreModuleNamespaces.WORKFLOW, KRADConstants.DetailTypes.ALL_DETAIL_TYPE,
                    KRADConstants.SystemGroupParameterNames.BLANKET_APPROVE_NO_REQUEST_IND);
            if (allowBlanketApproveNoRequest != null && allowBlanketApproveNoRequest.booleanValue()) {
                documentActions.add(KRADConstants.KUALI_ACTION_CAN_BLANKET_APPROVE);
            }
        }
        return documentActions;
    }


}
