package edu.cornell.kfs.ksr.web.struts;

import java.util.List;

import org.kuali.kfs.sys.document.web.struts.FinancialSystemTransactionalDocumentFormBase;

import edu.cornell.kfs.ksr.KSRConstants;

public class SecurityRequestDocumentForm extends FinancialSystemTransactionalDocumentFormBase {
    private static final long serialVersionUID = 4809922796567396276L;

    private Long securityGroupId;
    private String currentPrincipalId;
    protected List<TabRoleIndexes> tabRoleIndexes;

    public SecurityRequestDocumentForm() {
        super();
    }

    protected String getDefaultDocumentTypeName() {
        return KSRConstants.SECURITY_REQUEST_DOC_TYPE_NAME;
    }

    @SuppressWarnings({"rawtypes","unchecked"})
    public void setTabRoleIndexes(final List tabRoleIndexes) {
        this.tabRoleIndexes = tabRoleIndexes;
    }
    
    @SuppressWarnings("rawtypes")
    public List getTabRoleIndexes() {
        return tabRoleIndexes;
    }

    public static class TabRoleIndexes implements java.io.Serializable {

    	private static final long serialVersionUID = 6981150675755290581L;
    	
    	private Long tabId;
        private String tabName;
        private List<Integer> roleRequestIndexes;

        public Long getTabId() {
            return tabId;
        }

        public void setTabId(Long tabId) {
            this.tabId = tabId;
        }

        public String getTabName() {
            return tabName;
        }

        public void setTabName(String tabName) {
            this.tabName = tabName;
        }

        @SuppressWarnings("rawtypes")
        public List getRoleRequestIndexes() {
            return roleRequestIndexes;
        }

        @SuppressWarnings({"rawtypes","unchecked"})
        public void setRoleRequestIndexes(List roleRequestIndexes) {
            this.roleRequestIndexes = (List<Integer>) roleRequestIndexes;
        }
    }

    public Long getSecurityGroupId() {
        return securityGroupId;
    }

    public void setSecurityGroupId(Long securityGroupId) {
        this.securityGroupId = securityGroupId;
    }

    public String getCurrentPrincipalId() {
        return currentPrincipalId;
    }

    public void setCurrentPrincipalId(String currentPrincipalId) {
        this.currentPrincipalId = currentPrincipalId;
    }

}
