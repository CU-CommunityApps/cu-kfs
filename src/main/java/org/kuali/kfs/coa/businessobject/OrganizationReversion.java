/*
 * Copyright 2005 The Kuali Foundation
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl2.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kuali.kfs.coa.businessobject;

import java.util.LinkedHashMap;
import java.util.List;

import org.kuali.rice.kns.bo.Inactivateable;
import org.kuali.rice.kns.util.TypedArrayList;

/**
 * 
 */
public class OrganizationReversion extends Reversion implements Inactivateable {

    
    private String organizationCode;
    
    private Organization organization;
    private List<Organization> organizations; // This is only used by the "global" document
    private List<OrganizationReversionDetail> organizationReversionDetails;

    /**
     * Default constructor.
     */
    public OrganizationReversion() {
        organizations = new TypedArrayList(Organization.class);
        organizationReversionDetails = new TypedArrayList(OrganizationReversionDetail.class);
    }   

    public List<OrganizationReversionDetail> getOrganizationReversionDetails() {
        return organizationReversionDetails;
    }

    public void addOrganizationReversionDetail(OrganizationReversionDetail ord) {
        organizationReversionDetails.add(ord);
    }

    public void setOrganizationReversionDetails(List<OrganizationReversionDetail> organizationReversionDetails) {
        this.organizationReversionDetails = organizationReversionDetails;
    }

    public ReversionCategoryInfo getReversionDetail(String categoryCode) {
        for (OrganizationReversionDetail element : organizationReversionDetails) {
            if (element.getOrganizationReversionCategoryCode().equals(categoryCode)) {
                if (!element.isActive()) {
                    return null; // don't send back inactive details
                } else {
                    return element;
                }
            }
        }
        return null;
    }



    /**
     * Gets the organizationCode attribute.
     * 
     * @return Returns the organizationCode
     */
    public String getOrganizationCode() {
        return organizationCode;
    }

    /**
     * Sets the organizationCode attribute.
     * 
     * @param organizationCode The organizationCode to set.
     */
    public void setOrganizationCode(String organizationCode) {
        this.organizationCode = organizationCode;
    }


    

    /**
     * Gets the organization attribute.
     * 
     * @return Returns the organization
     */
    public List<Organization> getOrganizations() {
        return organizations;
    }

    /**
     * Sets the organization attribute.
     * 
     * @param organization The organization to set.
     * @deprecated
     */
    public void setOrganizations(List<Organization> organization) {
        this.organizations = organization;
    }

   

    /**
     * @see org.kuali.rice.kns.bo.BusinessObjectBase#toStringMapper()
     */
    protected LinkedHashMap toStringMapper() {
        
        LinkedHashMap m = super.toStringMapper();
        
        m.put("organizationCode", this.organizationCode);
        return m;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    /**
     * This method (a hack by any other name...) returns a string so that an organization reversion can have a link to view its own
     * inquiry page after a look up
     * 
     * @return the String "View Organization Reversion"
     */
    public String getOrganizationReversionViewer() {
        return "View Organization Reversion";
    }


    @Override
    public String getSourceAttribute() {
        
        return organizationCode;
    }
}
