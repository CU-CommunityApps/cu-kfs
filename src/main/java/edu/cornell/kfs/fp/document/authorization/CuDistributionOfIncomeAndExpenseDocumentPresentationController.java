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
    public Set<String> getDocumentActions(Document document) {
        Set<String> documentActions = super.getDocumentActions(document);

        DistributionOfIncomeAndExpenseDocument distributionOfIncomeAndExpenseDocument = (DistributionOfIncomeAndExpenseDocument) document;
        String docInError = distributionOfIncomeAndExpenseDocument.getDocumentHeader().getFinancialDocumentInErrorNumber();
        
        if (StringUtils.isNotBlank(docInError)) {
            Boolean allowBlanketApproveNoRequest = getParameterService().getParameterValueAsBoolean(
                    KFSConstants.CoreModuleNamespaces.KFS, KRADConstants.DetailTypes.DOCUMENT_DETAIL_TYPE,
                    KRADConstants.SystemGroupParameterNames.ALLOW_ENROUTE_BLANKET_APPROVE_WITHOUT_APPROVAL_REQUEST_IND);
            if (allowBlanketApproveNoRequest != null && allowBlanketApproveNoRequest.booleanValue()) {
                documentActions.add(KRADConstants.KUALI_ACTION_CAN_BLANKET_APPROVE);
            }
        }
        return documentActions;
    }

}
