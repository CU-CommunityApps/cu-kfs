/**
 * 
 */
package edu.cornell.kfs.sys.document.validation.impl;

import static org.kuali.rice.core.api.criteria.PredicateFactory.equal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.Organization;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.businessobject.TargetAccountingLine;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.kfs.sys.document.validation.GenericValidation;
import org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent;
import org.kuali.rice.core.api.criteria.PredicateUtils;
import org.kuali.rice.core.api.criteria.QueryByCriteria;
import org.kuali.rice.kim.api.KimConstants;
import org.kuali.rice.kim.api.role.Role;
import org.kuali.rice.kim.api.role.RoleMemberQueryResults;
import org.kuali.rice.kim.api.role.RoleMembership;
import org.kuali.rice.kim.api.role.RoleService;
import org.kuali.rice.kim.api.services.KimApiServiceLocator;
import org.kuali.rice.krad.util.GlobalVariables;

import edu.cornell.kfs.sys.CUKFSKeyConstants;

/**
 * @author Dennis Friends
 *
 */
public class OrgAssignedValidation extends GenericValidation {
	
	private AccountingDocument yearEndDocumentForValidation;
	
	public static final String FPYE_ROLE_ID = "100000206";
	public static final String LDYE_ROLE_ID = "100000207";
	
	/* (non-Javadoc)
	 * @see org.kuali.kfs.sys.document.validation.Validation#validate(org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent)
	 */
	public boolean validate(AttributedDocumentEvent event) {
		List<SourceAccountingLine> srcAcctingLines = yearEndDocumentForValidation.getSourceAccountingLines();
		List<TargetAccountingLine> tgtAcctingLines = yearEndDocumentForValidation.getTargetAccountingLines();
		
		List<Account> badAccounts = new ArrayList<Account> ();
		
		// Determine if the document is an FPYE doc or an LDYE document
		String docType = event.getDocument().getDocumentHeader().getWorkflowDocument().getDocumentTypeName();
		String yeRoleID = "";
		
		if(docType.equals("YEST") || docType.equals("YEBT")) {
			yeRoleID = LDYE_ROLE_ID;
		} else if(docType.equals("YEDI") || docType.equals("YEBA") || docType.equals("YETF") || docType.equals("YEGE")) {
			yeRoleID = FPYE_ROLE_ID;
		}
		
		// Examine accounts for each source accounting line
		for(SourceAccountingLine src : srcAcctingLines) {
			Account acct = src.getAccount();
			Organization org = acct.getOrganization();
			if(!hasOrgReviewer(acct, org, yeRoleID, true)) {
				badAccounts.add(src.getAccount());
			}
		}

		// Examine accounts for each target accounting line
		for(TargetAccountingLine tgt : tgtAcctingLines) {
			Account acct = tgt.getAccount();
			Organization org = acct.getOrganization();
			if(!hasOrgReviewer(acct, org, yeRoleID, true)) {
				badAccounts.add(tgt.getAccount());
			}
		}
		
		return badAccounts.isEmpty();
	}
	
	/**
	 * 
	 * @param orgCode
	 * @param chartCode
	 * @return
	 */
	private boolean hasOrgReviewer(Account account, Organization org, String documentTypeRoleID, boolean firstCall) {
		// use orgCode, chartCode and ACCT doc type to find org reviewers
		RoleService roleService = KimApiServiceLocator.getRoleService();
		Role role = roleService.getRole(documentTypeRoleID);
		
		// or roleservice.getRoleByName(namespaceCode, roleName)
		List<RoleMembership> roleMembers = roleService.findRoleMemberships(QueryByCriteria.Builder.fromPredicates(equal("ROLE_ID", role.getId()))).getResults();   		
		
		boolean hasReviewer = false;
		for(RoleMembership member : roleMembers) {
	        RoleMemberQueryResults members = KimApiServiceLocator.getRoleService().findRoleMembers(QueryByCriteria.Builder.fromPredicates( PredicateUtils.convertMapToPredicate(Collections.singletonMap(KimConstants.PrimaryKeyConstants.ID, member.getId()))));
            boolean isActiveMember = true;
            if ( members != null && CollectionUtils.isNotEmpty(members.getResults())) {
                isActiveMember = members.getResults().get(0).isActive();
            }

	        String memberOrg = member.getQualifier().get(KFSConstants.ORGANIZATION_CODE_PROPERTY_NAME);
			if(isActiveMember && org.getOrganizationCode().equalsIgnoreCase(memberOrg)) {
				hasReviewer = true;
				break;
			}
		}

		Organization rto = org.getReportsToOrganization();
		if(!hasReviewer && rto != null) {
			Organization reportsTo = org.getReportsToOrganization();
			if(!reportsTo.getOrganizationCode().equalsIgnoreCase("UNIV")) {
				hasReviewer = hasOrgReviewer(account, reportsTo, documentTypeRoleID, false);
			}
		}
		
		if(!hasReviewer && firstCall) {
            GlobalVariables.getMessageMap().putError(KFSConstants.ACCOUNTING_LINE_ERRORS, CUKFSKeyConstants.ERROR_YEAREND_DOCUMENT_MISSING_ORG_REVIEWER, account.getAccountNumber(), org.getOrganizationCode());
		}
		return hasReviewer;
	}

	/**
	 * @return the yearEndDocumentForValidation
	 */
	public AccountingDocument getYearEndDocumentForValidation() {
		return yearEndDocumentForValidation;
	}

	/**
	 * @param yearEndDocumentForValidation the yearEndDocumentForValidation to set
	 */
	public void setYearEndDocumentForValidation(AccountingDocument yearEndDocumentForValidation) {
		this.yearEndDocumentForValidation = yearEndDocumentForValidation;
	}

}
