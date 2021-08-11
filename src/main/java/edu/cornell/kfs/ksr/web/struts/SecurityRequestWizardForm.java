package edu.cornell.kfs.ksr.web.struts;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sys.document.web.struts.FinancialSystemTransactionalDocumentFormBase;

import edu.cornell.kfs.ksr.businessobject.SecurityGroup;


public class SecurityRequestWizardForm extends FinancialSystemTransactionalDocumentFormBase {
    private static final long serialVersionUID = 8896771325196119964L;

    private SecurityGroup securityGroup;
    private List<SecurityGroup> securityGroups;
    

    public SecurityRequestWizardForm() {
        super();
    }

    /**
     * Overriding to allow the process wizard call (since the wizard form is not
     * stored in session)
     * 
     * @see org.kuali.rice.kns.web.struts.form.KualiForm#shouldMethodToCallParameterBeUsed(java.lang.String,
     *      java.lang.String, javax.servlet.http.HttpServletRequest)
     */
    @Override
    public boolean shouldMethodToCallParameterBeUsed(String methodToCallParameterName, String methodToCallParameterValue,
            HttpServletRequest request) {
        if (StringUtils.equals(methodToCallParameterValue, "processWizard")) {
            return true;
        }

        return super.shouldMethodToCallParameterBeUsed(methodToCallParameterName, methodToCallParameterValue, request);
    }

    /**
     * @return the securityGroup
     */
    public SecurityGroup getSecurityGroup() {
        return securityGroup;
    }

    /**
     * @param securityGroup
     *            the securityGroup to set
     */
    public void setSecurityGroup(SecurityGroup securityGroup) {
        this.securityGroup = securityGroup;
    }

	/**
	 * @param securityGroups the securityGroups to set
	 */
	public void setSecurityGroups(List<SecurityGroup> securityGroups) {
		this.securityGroups = securityGroups;
	}

	/**
	 * @return the securityGroups
	 */
	public List<SecurityGroup> getSecurityGroups() {
		return securityGroups;
	}

}
