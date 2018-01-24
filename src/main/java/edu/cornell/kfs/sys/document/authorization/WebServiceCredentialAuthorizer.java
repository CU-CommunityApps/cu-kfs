package edu.cornell.kfs.sys.document.authorization;

import edu.cornell.kfs.sys.CUKFSParameterKeyConstants;
import edu.cornell.kfs.sys.businessobject.WebServiceCredential;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.authorization.FinancialSystemMaintenanceDocumentAuthorizerBase;
import org.kuali.rice.core.api.parameter.ParameterEvaluator;
import org.kuali.rice.core.api.parameter.ParameterEvaluatorService;
import org.kuali.rice.kim.api.identity.Person;

public class WebServiceCredentialAuthorizer extends FinancialSystemMaintenanceDocumentAuthorizerBase {

    @Override
    public boolean canMaintain(Object dataObject, Person user) {
        if(!super.canMaintain(dataObject, user)){
            return false;
        }

        WebServiceCredential webServiceCredential = (WebServiceCredential) dataObject;
        ParameterEvaluator parameterEvaluator = SpringContext.getBean(ParameterEvaluatorService.class).getParameterEvaluator(
                WebServiceCredential.class,
                CUKFSParameterKeyConstants.NON_EDITABLE_CREDENTIAL_VALUES,
                webServiceCredential.getCredentialGroupCode(),
                webServiceCredential.getCredentialKey()
        );

        //do not allow editing non-editable credential values
        return !parameterEvaluator.evaluationSucceeds();
    }

}
