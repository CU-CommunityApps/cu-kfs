package edu.cornell.kfs.sys.document.authorization;

import java.util.Set;

import org.kuali.kfs.coreservice.api.parameter.Parameter;
import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.module.cam.CamsConstants;
import org.kuali.kfs.module.cam.businessobject.Asset;
import org.kuali.kfs.module.cam.document.service.AssetService;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.authorization.FinancialSystemMaintenanceDocumentPresentationControllerBase;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.kuali.rice.kew.api.WorkflowDocument;
import org.kuali.rice.krad.bo.BusinessObject;

import edu.cornell.kfs.sys.businessobject.WebServiceCredential;
import edu.cornell.kfs.sys.service.WebServiceCredentialService;
public class WebServiceCredentialPresentationController extends FinancialSystemMaintenanceDocumentPresentationControllerBase {

    @Override
    public boolean canEdit(Document document) {
        WebServiceCredentialService webServiceCredentialService = SpringContext.getBean(WebServiceCredentialService.class);
        PersistableBusinessObject bo = ((MaintenanceDocument) document).getDocumentBusinessObject();
        return true;
    }


    @Override
    public Set<String> getDocumentActions(Document document) {
        Set<String> documentActions = super.getDocumentActions(document);

        PersistableBusinessObject bo = ((MaintenanceDocument) document).getDocumentBusinessObject();
        WebServiceCredential webServiceCredential = (WebServiceCredential) bo;

        return documentActions;
    }

    @Override
    public boolean canMaintain(Object dataObject){
        return true;
//        Parameter parameter =  getParameterService().getParameter(
//                KfsParameterConstants.FINANCIAL_SYSTEM_NAMESPACE,
//                KfsParameterConstants.ALL_COMPONENT,
//                CUKFSParameterKeyConstants.NON_EDITABLE_CREDENTIAL_VALUES);
//
        // String parameterValue = parameter.getValue();
        //
        //        return parameterValue.contains(webServiceCredential.getCredentialGroupCode())
        //        return parameterValue.contains(webServiceCredential.getCredentialValue());
    }
}
