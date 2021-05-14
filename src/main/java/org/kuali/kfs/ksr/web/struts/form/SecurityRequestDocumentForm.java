/*
 * Copyright 2007 The Kuali Foundation
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.kuali.kfs.ksr.web.struts.form;

import java.util.List;

import org.kuali.kfs.ksr.KsrConstants;
import org.kuali.rice.kns.web.struts.form.KualiTransactionalDocumentFormBase;

/**
 * ====
 * CU Customization (CYNERGY-2377):
 * Copied over the SecurityRequestDocumentForm class from a more up-to-date rSmart KSR repository,
 * since that file includes updates that previously did not exist in our KSR version.
 * 
 * CU Customization:
 * Remediated this class as needed for Rice 2.x compatibility.
 * Also deprecated this class; use the KRAD version of the SecurityRequestDocumentForm instead.
 * ====
 * 
 * Action form for the Security Request Document
 * 
 * @deprecated
 * @author rSmart Development Team
 */
@Deprecated
public class SecurityRequestDocumentForm extends KualiTransactionalDocumentFormBase {
    private static final long serialVersionUID = 4809922796567396276L;

    private Long securityGroupId;
    protected List<TabRoleIndexes> tabRoleIndexes;

    public SecurityRequestDocumentForm() {
        super();
    }

    protected String getDefaultDocumentTypeName() {
        return KsrConstants.SECURITY_REQUEST_DOC_TYPE_NAME;
    }

    /**
     * Setter method to accompany {@link #getTabRoleIndexes()} in order to complement a property
     *
     * @param tabRoleIndexes {@link List} of {@link TabRoleIndexes}
     */
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

        /**
         * @return the tabId
         */
        public Long getTabId() {
            return tabId;
        }

        /**
         * @param tabId
         *            the tabId to set
         */
        public void setTabId(Long tabId) {
            this.tabId = tabId;
        }

        /**
         * @return the tabName
         */
        public String getTabName() {
            return tabName;
        }

        /**
         * @param tabName
         *            the tabName to set
         */
        public void setTabName(String tabName) {
            this.tabName = tabName;
        }

        /**
         * @return the roleRequestIndexes
         */
        @SuppressWarnings("rawtypes")
        public List getRoleRequestIndexes() {
            return roleRequestIndexes;
        }

        /**
         * @param roleRequestIndexes
         *            the roleRequestIndexes to set
         */
        @SuppressWarnings({"rawtypes","unchecked"})
        public void setRoleRequestIndexes(List roleRequestIndexes) {
            this.roleRequestIndexes = (List<Integer>) roleRequestIndexes;
        }
    }

    /**
     * Selected security group id for the security request document
     * 
     * @return KualiInteger id for security group
     */
    public Long getSecurityGroupId() {
        return securityGroupId;
    }

    /**
     * Setter for the security request security group id
     * 
     * @param securityGroupId
     */
    public void setSecurityGroupId(Long securityGroupId) {
        this.securityGroupId = securityGroupId;
    }

}
