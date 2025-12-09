package edu.cornell.kfs.fp.document.authorization;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.fp.document.DistributionOfIncomeAndExpenseDocument;
import org.kuali.kfs.fp.document.authorization.DistributionOfIncomeAndExpenseDocumentPresentationController;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.sys.KFSConstants;

public class CuDistributionOfIncomeAndExpenseDocumentPresentationController extends DistributionOfIncomeAndExpenseDocumentPresentationController {
    @Override
    public Set<String> getDocumentActions(final Document document) {
        final Set<String> documentActions = super.getDocumentActions(document);

        final DistributionOfIncomeAndExpenseDocument distributionOfIncomeAndExpenseDocument = (DistributionOfIncomeAndExpenseDocument) document;
        final String docInError = distributionOfIncomeAndExpenseDocument.getDocumentHeader().getFinancialDocumentInErrorNumber();
        
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
