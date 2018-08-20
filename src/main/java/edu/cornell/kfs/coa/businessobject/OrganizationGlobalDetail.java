package edu.cornell.kfs.coa.businessobject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.businessobject.Chart;
import org.kuali.kfs.coa.businessobject.Organization;
import org.kuali.kfs.sys.context.SpringContext;

import org.kuali.kfs.krad.bo.GlobalBusinessObjectDetailBase;
import org.kuali.kfs.krad.service.PersistenceStructureService;
import org.kuali.kfs.krad.util.ObjectUtils;

/**
 * A Business Object representing a single org to be changed on an Organization Global document.
 * Based off of a similar BO for Account Global documents.
 */
public class OrganizationGlobalDetail extends GlobalBusinessObjectDetailBase {

    private static final long serialVersionUID = -6705447573528822348L;
    private static final Logger LOG = LogManager.getLogger(OrganizationGlobalDetail.class);

    private String chartOfAccountsCode;
    private String organizationCode;

    private transient Chart chartOfAccounts;
    private transient Organization organization;

    public OrganizationGlobalDetail() {
        
    }

    /**
     * Returns a map of the keys<propName,value> based on the primary key names of the underlying BO and reflecting into this
     * object.
     * This is a tweaked copy of a method from the KFS AccountGlobalDetail class.
     */
    public Map<String,Object> getPrimaryKeys() {
        try {
            @SuppressWarnings("unchecked")
            List<String> keys = SpringContext.getBean(PersistenceStructureService.class).getPrimaryKeys(Organization.class);
            HashMap<String, Object> pks = new HashMap<String, Object>(keys.size());
            for (String key : keys) {
                // attempt to read the property of the current object
                // this requires that the field names match between the underlying BO object
                // and this object
                pks.put(key, ObjectUtils.getPropertyValue(this, key));
            }
            return pks;
        } catch (Exception ex) {
            LOG.error("unable to get primary keys for global detail object", ex);
        }
        return new HashMap<String, Object>(0);
    }

    public String getChartOfAccountsCode() {
        return chartOfAccountsCode;
    }

    public void setChartOfAccountsCode(String chartOfAccountsCode) {
        this.chartOfAccountsCode = chartOfAccountsCode;
    }

    public String getOrganizationCode() {
        return organizationCode;
    }

    public void setOrganizationCode(String organizationCode) {
        this.organizationCode = organizationCode;
    }

    public Chart getChartOfAccounts() {
        return chartOfAccounts;
    }

    public void setChartOfAccounts(Chart chartOfAccounts) {
        this.chartOfAccounts = chartOfAccounts;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

}
