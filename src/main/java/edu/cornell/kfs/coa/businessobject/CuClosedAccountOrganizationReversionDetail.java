package edu.cornell.kfs.coa.businessobject;

import org.kuali.kfs.sys.KFSConstants;


/**
 * Returns OrganizationReversionCategoryInformation for a closed account - which is to say that only
 * returns "R2" as the organization reversion strategy code
 */
public class CuClosedAccountOrganizationReversionDetail implements ReversionCategoryInfo {
    private ReversionCategoryInfo reversionCategoryInfo;
    
    /**
     * Constructs a ClosedAccountOrganizationReversionDetail.java.
     * @param organizationReversionDetail the decorated organization reversion detail
     */
    protected CuClosedAccountOrganizationReversionDetail(ReversionCategoryInfo organizationReversionDetail) {
        this.reversionCategoryInfo = organizationReversionDetail;
    }

    /**
     * Always returns R2
     * @see org.kuali.kfs.coa.businessobject.OrganizationReversionCategoryInfo#getOrganizationReversionCode()
     */
    public String getReversionCode() {
        return KFSConstants.RULE_CODE_R2;
    }

    /**
     * 
     * @see org.kuali.kfs.coa.businessobject.OrganizationReversionCategoryInfo#getOrganizationReversionObjectCode()
     */
    public String getReversionObjectCode() {
        return reversionCategoryInfo.getReversionObjectCode();
    }

}
