package edu.cornell.kfs.sys.document.authorization;

import edu.cornell.kfs.sys.CUKFSParameterKeyConstants;
import edu.cornell.kfs.sys.businessobject.WebServiceCredential;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.authorization.FinancialSystemMaintenanceDocumentAuthorizerBase;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.kuali.rice.kim.api.identity.Person;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class WebServiceCredentialAuthorizer extends FinancialSystemMaintenanceDocumentAuthorizerBase {

    @Override //do not allow editing non-editable credential values
    public boolean canMaintain(Object dataObject, Person user) {
        if(!super.canMaintain(dataObject, user)){
            return false;
        }

        //expected parameter string format "<group>=<comma_separated_list>;<group>=<comma_separated_list>;"
        WebServiceCredential webServiceCredential = (WebServiceCredential) dataObject;
        String nonEditableCredentialValues = SpringContext.getBean(ParameterService.class).getParameterValueAsString(
                KfsParameterConstants.FINANCIAL_SYSTEM_NAMESPACE,
                KfsParameterConstants.ALL_COMPONENT,
                CUKFSParameterKeyConstants.NON_EDITABLE_CREDENTIAL_VALUES);

        List<String> split = Arrays.asList(nonEditableCredentialValues.split(";"));
        String credentialGroupCodeSearch = webServiceCredential.getCredentialGroupCode() + "=";
        List<String> credentialGroupSection = split.stream()
                .filter(s -> s.startsWith(credentialGroupCodeSearch))
                .collect(Collectors.toList());
        if(credentialGroupSection.isEmpty()){
            return false;
        }

        String groupTokenCommaSeparatedStrings = credentialGroupSection.get(0).substring(credentialGroupCodeSearch.length());
        List<String> groupTokenList = Arrays.asList(groupTokenCommaSeparatedStrings.split(","));

        return !groupTokenList.contains(webServiceCredential.getCredentialKey());
    }

}
