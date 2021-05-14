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
package org.kuali.kfs.ksr.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.ksr.KsrConstants;
import org.kuali.kfs.ksr.bo.SecurityGroup;
import org.kuali.kfs.ksr.bo.SecurityGroupTab;
import org.kuali.kfs.ksr.bo.SecurityProvisioningGroup;
import org.kuali.kfs.ksr.bo.SecurityRequestRole;
import org.kuali.kfs.ksr.bo.SecurityRequestRoleQualification;
import org.kuali.kfs.ksr.bo.SecurityRequestRoleQualificationDetail;
import org.kuali.kfs.ksr.document.SecurityRequestDocument;
import org.kuali.kfs.ksr.exception.SecurityRequestDocumentException;
import org.kuali.kfs.ksr.service.SecurityRequestDocumentService;
import org.kuali.kfs.ksr.util.KsrUtil;
import org.kuali.rice.core.api.criteria.PredicateFactory;
import org.kuali.rice.core.api.criteria.QueryByCriteria;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.api.identity.PersonService;
import org.kuali.rice.kim.api.role.RoleService;
import org.kuali.rice.kim.api.services.KimApiServiceLocator;
import org.kuali.rice.kim.api.type.KimType;
import org.kuali.rice.kim.api.type.KimTypeAttribute;
import org.kuali.rice.krad.data.DataObjectService;
import org.kuali.rice.krad.util.KRADPropertyConstants;

/**
 * ====
 * CU Customizations:
 * 
 * Fixed this class to ensure that the strings used to test for changes
 * in request role qualifications are being generated correctly.
 * Specifically, the class will now only look at the user's direct qualifications.
 * 
 * Added isActive() checks on the group tabs and provisioning groups
 * when initiating a new security request document.
 * 
 * Updated KSR doc initialization to always use the new user's primary department code.
 * 
 * Also updated this file as needed for Rice 2.x compatibility.
 * ====
 * 
 * Implementation of <code>SecurityRequestDocumentService</code>
 * 
 * @author rSmart Development Team
 */
public class SecurityRequestDocumentServiceImpl implements
		SecurityRequestDocumentService {
	private static Logger LOG = LogManager.getLogger(SecurityRequestDocumentServiceImpl.class);

    protected PersonService personService;
    protected DataObjectService dataObjectService;

	/**
	 * Initiates a new <code>SecurityRequestDocument</code> instance
	 * 
	 * <p>
	 * Person record for given principal id is retrieved to set primary department code, then for each provisioning record on the selected security group, a new <code>SecurityRequestRole</code>
	 * instance is created and added to the document. As the request role lines are being constructed, the principal's current role membership status and qualifications (if any) are retrieved and set
	 * on the request
	 * </p>
	 * 
	 * @see org.kuali.kfs.ksr.service.SecurityRequestDocumentService#initiateSecurityRequestDocument(org.kuali.kfs.ksr.document.SecurityRequestDocument, org.kuali.rice.kim.bo.Person)
	 */
	@Override
	public void initiateSecurityRequestDocument(SecurityRequestDocument document, Person user) {
		LOG.info("Preparing security request document: "
				+ document.getDocumentNumber());

		String principalId = document.getPrincipalId();

		// if request principal or security group is empty we cannot do any
		// setup
		if (StringUtils.isBlank(principalId)) {
			throw new RuntimeException("Principal id not set for new Security Request Document");
		}
		else if (document.getSecurityGroupId() == null) {
			throw new RuntimeException("Security group id not set for new Security Request Document");
		}

		Person person = personService.getPerson(principalId);
		if (person != null) {
			document.setRequestPerson(person);
			document.setPrimaryDepartmentCode(person.getPrimaryDepartmentCode());
		}
		else {
			LOG.error("Unable to find person record for principal id: "
					+ principalId);
			throw new SecurityRequestDocumentException("Error preparing document: Unable to find person record for principal id: "
					+ principalId);
		}

		buildSecurityRequestRoles(document);
	}

	/**
	 * Checks status of document and if in the Saved status, prepares new qualification lines for adding. Invoked after a document instance is loaded from the database
	 * 
	 * @see org.kuali.kfs.ksr.service.SecurityRequestDocumentService#prepareSecurityRequestDocument(org.kuali.kfs.ksr.document.SecurityRequestDocument)
	 */
	@Override
	public void prepareSecurityRequestDocument(SecurityRequestDocument document) {
		if (document.getDocumentHeader().getWorkflowDocument().isSaved()) {
			for (SecurityRequestRole requestRole : document.getSecurityRequestRoles()) {
				SecurityRequestRoleQualification newRequestRoleQualification = buildRoleQualificationLine(requestRole, null);
				requestRole.setNewRequestRoleQualification(newRequestRoleQualification);
			}
		}
	}

	/**
	 * Builds the <code>SecurityRequestRole</code> collection for the document
	 * 
	 * <p>
	 * The configured provisioning records for the selected security group are retrieved and iterated over. For each provisioning record, a new <code>SecurityRequestRole</code> instance is created and
	 * {@link #buildSecurityRequestRoleQualifications(SecurityRequestRole, String)} is invoked to build the qualification details
	 * </p>
	 * 
	 * @param document
	 *            - security request document to build request role collection for
	 */
	protected void buildSecurityRequestRoles(SecurityRequestDocument document) {
		List<SecurityRequestRole> requestRoles = new ArrayList<SecurityRequestRole>();

		// sequence for new request role records
		long roleRequestId = 1;

		for (SecurityGroupTab groupTab : document.getSecurityGroup().getSecurityGroupTabs()) {
			if (groupTab.isActive()) {
				for (SecurityProvisioningGroup provisioningGroup : groupTab.getSecurityProvisioningGroups()) {
					if (provisioningGroup.isActive()) {
						SecurityRequestRole requestRole = new SecurityRequestRole();

						requestRole.setDocumentNumber(document.getDocumentNumber());
						requestRole.setRoleId(provisioningGroup.getRoleId());
						requestRole.setRoleRequestId(Long.valueOf(roleRequestId));

						buildSecurityRequestRoleQualifications(requestRole, document.getPrincipalId());

						roleRequestId++;
						requestRoles.add(requestRole);
					}
				}
			}
		}

		document.setSecurityRequestRoles(requestRoles);
	}

	/**
	 * Builds the <code>SecurityRequestRoleQualification</code> collection for the given request role
	 * 
	 * <p>
	 * Retrieves principal's membership status and qualifications (if any) for the role given on the request role instance. For each qualification set found, a
	 * <code>SecurityRequestRoleQualification</code> instance is created that holds the qualification details. In addition if the the role is qualified, a request role qualification line is created
	 * with blank attribute values for adding a new qualification set
	 * </p>
	 * 
	 * @param requestRole
	 *            - security request role instance to create qualifications for
	 * @param principalId
	 *            - principal id for which the request is being made
	 */
	protected void buildSecurityRequestRoleQualifications(SecurityRequestRole requestRole, String principalId) {
		// check user had role for setting the request active indicator
		List<String> roleIds = new ArrayList<String>();
		roleIds.add(requestRole.getRoleId());

		RoleService roleService = KimApiServiceLocator.getRoleService();

		boolean hasRole = roleService.principalHasRole(principalId, roleIds, Collections.emptyMap());
		if (hasRole) {
			requestRole.setActive(true);
			requestRole.setCurrentActive(true);
			requestRole.setCurrentQualifications("");

			// if qualified role, get principal's qualifications values for type attributes
			if (requestRole.isQualifiedRole()) {
				List<Map<String,String>> principalQualifications = roleService.getRoleQualifersForPrincipalByRoleIds(principalId, roleIds, Collections.emptyMap());

				List<SecurityRequestRoleQualification> requestQualifications = new ArrayList<SecurityRequestRoleQualification>();
				for (Map<String,String> qualification : principalQualifications) {
					SecurityRequestRoleQualification requestQualification = buildRoleQualificationLine(requestRole, qualification);

					requestQualifications.add(requestQualification);
				}

				requestRole.setRequestRoleQualifications(requestQualifications);
				buildCurrentQualificationsString(requestRole, principalId, principalQualifications);
			}
		}
		else {
			requestRole.setActive(false);
			requestRole.setCurrentActive(false);
		}

		// if qualified role, setup line for adding new qualification
		if (requestRole.isQualifiedRole()) {
			SecurityRequestRoleQualification newRequestRoleQualification = buildRoleQualificationLine(requestRole, null);
			requestRole.setNewRequestRoleQualification(newRequestRoleQualification);
		}
	}

	/**
	 * Builds a String representation for the principal's qualifications (if any) for the role on the given security request role instance
	 * 
	 * @param requestRole
	 *            - security request role instance to retrieve qualifications for
	 * @param principalId
	 *            - principal id for which the request is being made
	 */
	protected void buildCurrentQualificationsString(SecurityRequestRole requestRole, String principalId, List<Map<String,String>> principalQualifications) {
		List<KimTypeAttribute> typeAttributes = KsrUtil.getTypeAttributesForRoleRequest(requestRole);

		List<String> roleIds = new ArrayList<String>();
		roleIds.add(requestRole.getRoleId());

		if (principalQualifications == null) {
			principalQualifications = KimApiServiceLocator.getRoleService().getRoleQualifersForPrincipalByRoleIds(
			        principalId, roleIds, Collections.emptyMap());
		}

		String currentQualificationsString = KsrUtil.buildQualificationString(principalQualifications, typeAttributes);
		requestRole.setCurrentQualifications(currentQualificationsString);
	}

	/**
	 * Builds a <code>SecurityRequestRoleQualification</code> instance for the given security request role and the given qualification set
	 * 
	 * @see org.kuali.kfs.ksr.service.SecurityRequestDocumentService#buildRoleQualificationLine(org.kuali.kfs.ksr.bo.SecurityRequestRole, org.kuali.rice.kim.bo.types.dto.AttributeSet)
	 */
	@Override
	public SecurityRequestRoleQualification buildRoleQualificationLine(SecurityRequestRole requestRole, Map<String,String> qualification) {
		SecurityRequestRoleQualification requestQualification = new SecurityRequestRoleQualification();

		requestQualification.setDocumentNumber(requestRole.getDocumentNumber());
		requestQualification.setRoleRequestId(requestRole.getRoleRequestId());

		KimType typeInfo = KsrUtil.getTypeInfoForRoleRequest(requestRole);

		List<KimTypeAttribute> typeAttributes = KsrUtil.getTypeAttributesForRoleRequest(requestRole);
		for (KimTypeAttribute attributeInfo : typeAttributes) {
			SecurityRequestRoleQualificationDetail requestQualificationDetail = new SecurityRequestRoleQualificationDetail();

			requestQualificationDetail.setDocumentNumber(requestRole.getDocumentNumber());
			requestQualificationDetail.setRoleRequestId(requestRole.getRoleRequestId());
			requestQualificationDetail.setQualificationId(requestRole.getNextQualificationId());
			requestQualificationDetail.setAttributeId(attributeInfo.getKimAttribute().getId());
			requestQualificationDetail.setRoleTypeId(typeInfo.getId());

			if ((qualification != null)
					&& qualification.containsKey(attributeInfo.getKimAttribute().getAttributeName())) {
				requestQualificationDetail.setAttributeValue(qualification.get(attributeInfo.getKimAttribute().getAttributeName()));
			}

			requestQualification.getRoleQualificationDetails().add(requestQualificationDetail);
		}

		requestQualification.setQualificationId(requestRole.getNextQualificationId());
		requestRole.setNextQualificationId(requestRole.getNextQualificationId() + 1);

		return requestQualification;
	}

    @Override
    public List<SecurityGroup> getActiveSecurityGroups() {
        QueryByCriteria.Builder criteria = QueryByCriteria.Builder.create();
        criteria.setPredicates(
                PredicateFactory.equal(KRADPropertyConstants.ACTIVE, Boolean.TRUE));
        criteria.setOrderByAscending(KsrConstants.SECURITY_GROUP_NAME);
        
        return dataObjectService.findMatching(SecurityGroup.class, criteria.build())
                .getResults();
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    public void setDataObjectService(DataObjectService dataObjectService) {
        this.dataObjectService = dataObjectService;
    }

}
