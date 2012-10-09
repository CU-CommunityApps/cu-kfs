/**
 * 
 */
package edu.cornell.kfs.coa.service.impl;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.kuali.kfs.coa.businessobject.Organization;
import org.kuali.kfs.coa.service.OrganizationService;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.kns.util.ObjectUtils;

import au.com.bytecode.opencsv.CSVReader;
import edu.cornell.kfs.coa.service.OrgHierarchyMissingOrgsReportService;

/**
 * @author kwk43
 *
 */
public class OrgHierarchyMissingOrgsReportServiceImpl implements OrgHierarchyMissingOrgsReportService {
	
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(OrgHierarchyMissingOrgsReportServiceImpl.class);

    private OrganizationService orgService;
	
	public Set<Organization> findMissing(File baseOrgs, File orgHierarchies) {
		orgService = SpringContext.getBean(OrganizationService.class);
		List<Organization> orgsList = new ArrayList<Organization>();
		List<Organization> orgHierarchiesList = new ArrayList<Organization>();
		Set<Organization> orgsWithNoReview = null;;
		
		try {
			CSVReader reader = new CSVReader(new FileReader(baseOrgs), '\t');
			List<String[]> baseOrgList = reader.readAll();
			
			reader = new CSVReader(new FileReader(orgHierarchies), '\t');
			List<String[]> orgHierarchyList = reader.readAll();

		//	LOG.info("Read: "+ baseOrgList.toArray().length+" records");
			for (String[] line : baseOrgList) {
				String chart = line[0];
				String orgCode = line[1];

				Organization org = orgService.getByPrimaryId(chart, orgCode); 
				if (ObjectUtils.isNotNull(org)) {
					orgsList.add(org);
				}
		
			}
			String orgHierarchyChart = "";
			String orgHierarchyOrg = "";
			for (int i = 0; i < orgHierarchyList.toArray().length; i++) {
				String[] record = orgHierarchyList.get(i);

				if (i % 2 == 0) { // chart
					orgHierarchyChart = record[2];
				} else {
					orgHierarchyOrg = record[2];
					
					Organization org = orgService.getByPrimaryId(orgHierarchyChart, orgHierarchyOrg); 
					if (ObjectUtils.isNotNull(org)) {
						//LOG.info(org + " added to orgs with review.");
						orgHierarchiesList.add(org);
					}
					
				}
			}
			
			orgsWithNoReview = checkOrgHierarchy(orgsList, orgHierarchiesList);
			System.out.println(orgsWithNoReview.size());
		
		} catch (Exception e) {
			e.printStackTrace();
		}
        	return orgsWithNoReview;
	}
	
	/*
	 * 
	 * The logic would be to start with the lowest level orgs and: 
		1. Check to see if this org has org review set up, if yes, this org is ok, do not return in the report. If no, go to step 2 
		2. Identify the parent of the org in the previous step 
		3. Check to see if the parent org has org review set up if yes, this org is ok, do not return in the report. If no, return to step 2 
		4. Provide a report of all orgs that do not have any org review in the hierarchy 

	 */
	
	protected Set<Organization> checkOrgHierarchy(List<Organization> orgsList, List<Organization> orgHierarchiesList) {
		Set<Organization> orgsWithNoReview = new HashSet<Organization>();
		
		for (Organization org : orgsList) {
		//	if (!orgHierarchiesList.contains(org)) { // step 1
				// step 2
			//	Organization parent = org.getReportsToOrganization();

			//	LOG.info("Org Review NOT set up for Org: "+ org +". Checking reports to Org: "+ parent.getOrganizationCode());
//				while (!orgHierarchiesList.contains(parent) && !parent.getOrganizationTypeCode().equals("U")) {
//				//	LOG.info("Org Review NOT set up for Org: "+ parent +". Checking reports to Org: "+ parent.getReportsToOrganizationCode());
//
//					parent = parent.getReportsToOrganization();
//				}
				
			//	LOG.info("Checking Org: "+ org);
				if (!hasOrgReview(false, org, orgHierarchiesList)) {
					//if (org.getOrganizationCode().equals("S") || org.getOrganizationCode().equals("D")) {
					if (org.isActive())
						orgsWithNoReview.add(org);
					
						//LOG.info("Org Review NOT set up for "+ org);
					//}
				} else {
				//	LOG.info("Org Review set up for "+ org);
				}
					
				
		//	} else {
			//	LOG.info("Org Review set up for "+ org);
		//	}
		}
		for (java.util.Iterator<Organization> itr = orgsWithNoReview.iterator(); itr.hasNext();) {
			Organization org = itr.next();
			System.out.println(org.getChartOfAccountsCode()+","+org.getOrganizationCode()+","+org.getReportsToChartOfAccountsCode()+","+org.getReportsToOrganizationCode()+","+!orgService.getActiveAccountsByOrg(org.getChartOfAccountsCode(),org.getOrganizationCode()).isEmpty());

		}
		
		return orgsWithNoReview;
	}
	
	private boolean hasOrgReview(boolean retVal, Organization org, List<Organization> orgHierarchiesList) {
		//LOG.info("ORG: "+ org + " TYP: "+org.getOrganizationTypeCode()+ " reVal: "+ retVal);
//		if (!org.getOrganizationTypeCode().equals("U"))
//			LOG.info("ORG: "+ org + " TYP: "+org.getOrganizationTypeCode()+ " reVal: "+ retVal);
		
		if (orgHierarchiesList.contains(org)){
			retVal = true;
		}
		if ( !org.getOrganizationTypeCode().equals("U") && !orgHierarchiesList.contains(org)  && !retVal) {
			//boolean temp = 
			retVal |= hasOrgReview(retVal, org.getReportsToOrganization(), orgHierarchiesList);
		}  
		//retVal = true;
	//	System.out.println(retVal);
		return retVal;
	}

}
