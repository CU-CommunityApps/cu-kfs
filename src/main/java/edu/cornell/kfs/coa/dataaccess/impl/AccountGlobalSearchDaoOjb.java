/*
 * Copyright 2011 The Kuali Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.cornell.kfs.coa.dataaccess.impl;

import edu.cornell.kfs.coa.dataaccess.AccountGlobalSearchDao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryFactory;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.Organization;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.core.framework.persistence.ojb.dao.PlatformAwareDaoBaseOjb;
import org.kuali.kfs.krad.dao.BusinessObjectDao;
import org.kuali.kfs.krad.dao.impl.LookupDaoOjb;

public class AccountGlobalSearchDaoOjb extends PlatformAwareDaoBaseOjb implements AccountGlobalSearchDao {
    private LookupDaoOjb lookupDao;
    private BusinessObjectDao businessObjectDao;

    public Collection getAccountsByOrgHierarchy(String chartOfAccountsCode, String organizationCode, Map<String, String> parameters) {
        
        Criteria criteria = new Criteria();
        Criteria orgCriteria = new Criteria();

        orgCriteria.addEqualTo(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, chartOfAccountsCode);
        orgCriteria.addEqualTo(KFSPropertyConstants.ORGANIZATION_CODE, organizationCode);
        criteria.addOrCriteria(orgCriteria);

        List<Organization> reportingOrgs = getReportingOrgs(chartOfAccountsCode, organizationCode, new HashSet<Organization>());
        for (Organization reportingOrg : reportingOrgs) {
            orgCriteria = new Criteria();
            orgCriteria.addEqualTo(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, reportingOrg.getChartOfAccountsCode());
            orgCriteria.addEqualTo(KFSPropertyConstants.ORGANIZATION_CODE, reportingOrg.getOrganizationCode());
            criteria.addOrCriteria(orgCriteria);
        }
        
        //Add the rest of the criteria for the lookup
        parameters.remove(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE);
        parameters.remove(KFSPropertyConstants.ORGANIZATION_CODE);
        criteria.addAndCriteria(lookupDao.getCollectionCriteriaFromMap(new Account(), parameters));
        
        return getPersistenceBrokerTemplate().getCollectionByQuery(QueryFactory.newQuery(Account.class, criteria));
    }

    protected List<Organization> getReportingOrgs(String chartOfAccountsCode, String organizationCode, Set<Organization> seenOrgs) {
        List<Organization> reportingOrgs = new ArrayList<Organization>();

        Map<String, String> reportsToCriteria = new HashMap<String, String>();
        reportsToCriteria.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, chartOfAccountsCode);
        reportsToCriteria.put(KFSPropertyConstants.REPORTS_TO_ORGANIZATION_CODE, organizationCode);
        
        Collection<Organization> childOrgs = businessObjectDao.findMatching(Organization.class, reportsToCriteria);
        for (Organization childOrg : childOrgs) {
            if (!seenOrgs.contains(childOrg)) {
                seenOrgs.add(childOrg);

                reportingOrgs.add(childOrg);
                reportingOrgs.addAll(getReportingOrgs(childOrg.getChartOfAccountsCode(), childOrg.getOrganizationCode(), seenOrgs));
            }
        }

        return reportingOrgs;
    }

    protected LookupDaoOjb getLookupDao() {
        return lookupDao;
    }

    public void setLookupDao(LookupDaoOjb lookupDao) {
        this.lookupDao = lookupDao;
    }

    public BusinessObjectDao getBusinessObjectDao() {
        return businessObjectDao;
    }

    public void setBusinessObjectDao(BusinessObjectDao businessObjectDao) {
        this.businessObjectDao = businessObjectDao;
    }
    
}
