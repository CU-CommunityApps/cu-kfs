package edu.cornell.kfs.sys.document.authorization;

import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.kns.document.authorization.MaintenanceDocumentAuthorizerBase;
import org.kuali.kfs.core.api.parameter.ParameterEvaluator;
import org.kuali.kfs.core.api.parameter.ParameterEvaluatorService;
import org.kuali.kfs.kim.impl.identity.Person;

import edu.cornell.kfs.sys.CUKFSParameterKeyConstants;
import edu.cornell.kfs.sys.businessobject.WebServiceCredential;

public class WebServiceCredentialAuthorizer extends MaintenanceDocumentAuthorizerBase {

    private ParameterEvaluatorService parameterEvaluatorService;

    @Override
    public boolean canMaintain(Object dataObject, Person user) {
        if (!super.canMaintain(dataObject, user)) {
            return false;
        }

        WebServiceCredential webServiceCredential = (WebServiceCredential) dataObject;
        ParameterEvaluator parameterEvaluator = getParameterEvaluatorService().getParameterEvaluator(
            WebServiceCredential.class,
            CUKFSParameterKeyConstants.NON_EDITABLE_CREDENTIAL_VALUES,
            webServiceCredential.getCredentialGroupCode(),
            webServiceCredential.getCredentialKey()
        );

        return !parameterEvaluator.evaluationSucceeds();
    }

    public ParameterEvaluatorService getParameterEvaluatorService() {
        if (parameterEvaluatorService == null) {
            setParameterEvaluatorService(SpringContext.getBean(ParameterEvaluatorService.class));
        }
        return parameterEvaluatorService;
    }

    public void setParameterEvaluatorService(ParameterEvaluatorService parameterEvaluatorService) {
        this.parameterEvaluatorService = parameterEvaluatorService;
    }

}
