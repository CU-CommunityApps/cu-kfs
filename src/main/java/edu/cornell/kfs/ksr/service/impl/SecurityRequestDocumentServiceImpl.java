package edu.cornell.kfs.ksr.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kuali.kfs.krad.service.BusinessObjectService;

import edu.cornell.kfs.ksr.KSRPropertyConstants;
import edu.cornell.kfs.ksr.businessobject.SecurityGroup;
import edu.cornell.kfs.ksr.service.SecurityRequestDocumentService;

public class SecurityRequestDocumentServiceImpl implements SecurityRequestDocumentService {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(SecurityRequestDocumentServiceImpl.class);

    // private PersonService personService;
    private BusinessObjectService businessObjectService;

    /**
     * Initiates a new <code>SecurityRequestDocument</code> instance
     * 
     * <p>
     * Person record for given principal id is retrieved to set primary department code, then for each provisioning record on the selected security group, a new
     * <code>SecurityRequestRole</code> instance is created and added to the document. As the request role lines are being constructed, the principal's current
     * role membership status and qualifications (if any) are retrieved and set on the request
     * </p>
     * 
     * @see org.kuali.rice.ksr.service.SecurityRequestDocumentService#initiateSecurityRequestDocument(org.kuali.rice.ksr.document.SecurityRequestDocument,
     *      org.kuali.rice.kim.bo.Person)
     */
    // @Override
    // public void initiateSecurityRequestDocument(SecurityRequestDocument document, Person user) {
    // LOG.info("Preparing security request document: "
    // + document.getDocumentNumber());
    //
    // String principalId = document.getPrincipalId();
    //
    // // if request principal or security group is empty we cannot do any
    // // setup
    // if (StringUtils.isBlank(principalId)) {
    // throw new RuntimeException("Principal id not set for new Security Request Document");
    // }
    // else if (document.getSecurityGroupId() == null) {
    // throw new RuntimeException("Security group id not set for new Security Request Document");
    // }
    //
    // // get primary department for principal
    // // ==== CU Customization (CYNERGY-2509): Commented out the outer "if" block. ====
    // //if (StringUtils.isBlank(document.getPrimaryDepartmentCode())) {
    // Person person = getPersonService().getPerson(principalId);
    // if (person != null) {
    // document.setRequestPerson(person);
    // document.setPrimaryDepartmentCode(person.getPrimaryDepartmentCode());
    // }
    // else {
    // LOG.error("Unable to find person record for principal id: "
    // + principalId);
    // throw new SecurityRequestDocumentException("Error preparing document: Unable to find person record for principal id: "
    // + principalId);
    // }
    // //}
    //
    // buildSecurityRequestRoles(document);
    // }
    //
    // /**
    // * Checks status of document and if in the Saved status, prepares new qualification lines for adding. Invoked after a document instance is loaded from the
    // database
    // *
    // * @see org.kuali.rice.ksr.service.SecurityRequestDocumentService#prepareSecurityRequestDocument(org.kuali.rice.ksr.document.SecurityRequestDocument)
    // */
    // @Override
    // public void prepareSecurityRequestDocument(SecurityRequestDocument document) {
    // if (document.getDocumentHeader().getWorkflowDocument().isSaved()) {
    // for (SecurityRequestRole requestRole : document.getSecurityRequestRoles()) {
    // SecurityRequestRoleQualification newRequestRoleQualification = buildRoleQualificationLine(requestRole, null);
    // requestRole.setNewRequestRoleQualification(newRequestRoleQualification);
    // }
    // }
    // }
    //
    // /**
    // * Builds the <code>SecurityRequestRole</code> collection for the document
    // *
    // * <p>
    // * The configured provisioning records for the selected security group are retrieved and iterated over. For each provisioning record, a new
    // <code>SecurityRequestRole</code> instance is created and
    // * {@link #buildSecurityRequestRoleQualifications(SecurityRequestRole, String)} is invoked to build the qualification details
    // * </p>
    // *
    // * @param document
    // * - security request document to build request role collection for
    // */
    // protected void buildSecurityRequestRoles(SecurityRequestDocument document) {
    // List<SecurityRequestRole> requestRoles = new ArrayList<SecurityRequestRole>();
    //
    // // sequence for new request role records
    // KualiInteger roleRequestId = new KualiInteger(1);
    //
    // // ==== CU Customization (CYNERGY-2379): Added isActive() checks for the group tabs and provisioning groups. ====
    // for (SecurityGroupTab groupTab : document.getSecurityGroup().getSecurityGroupTabs()) {
    // if (groupTab.isActive()) {
    // for (SecurityProvisioningGroup provisioningGroup : groupTab.getSecurityProvisioningGroups()) {
    // if (provisioningGroup.isActive()) {
    // SecurityRequestRole requestRole = new SecurityRequestRole();
    //
    // requestRole.setDocumentNumber(document.getDocumentNumber());
    // requestRole.setRoleId(provisioningGroup.getRoleId());
    // requestRole.setRoleRequestId(roleRequestId);
    //
    // buildSecurityRequestRoleQualifications(requestRole, document.getPrincipalId());
    //
    // roleRequestId = roleRequestId.add(new KualiInteger(1));
    // requestRoles.add(requestRole);
    // }
    // }
    // }
    // }
    //
    // document.setSecurityRequestRoles(requestRoles);
    // }
    //
    // /**
    // * Builds the <code>SecurityRequestRoleQualification</code> collection for the given request role
    // *
    // * <p>
    // * Retrieves principal's membership status and qualifications (if any) for the role given on the request role instance. For each qualification set found,
    // a
    // * <code>SecurityRequestRoleQualification</code> instance is created that holds the qualification details. In addition if the the role is qualified, a
    // request role qualification line is created
    // * with blank attribute values for adding a new qualification set
    // * </p>
    // *
    // * @param requestRole
    // * - security request role instance to create qualifications for
    // * @param principalId
    // * - principal id for which the request is being made
    // */
    // protected void buildSecurityRequestRoleQualifications(SecurityRequestRole requestRole, String principalId) {
    // // check user had role for setting the request active indicator
    // List<String> roleIds = new ArrayList<String>();
    // roleIds.add(requestRole.getRoleId());
    //
    // RoleService roleService = KimApiServiceLocator.getRoleService();
    //
    // boolean hasRole = roleService.principalHasRole(principalId, roleIds, null);
    // if (hasRole) {
    // requestRole.setActive(true);
    // requestRole.setCurrentActive(true);
    // requestRole.setCurrentQualifications("");
    //
    // // if qualified role, get principal's qualifications values for type attributes
    // if (requestRole.isQualifiedRole()) {
    // // ==== CU Customization: Changed third arg to an empty map. ====
    // List<Map<String,String>> principalQualifications = roleService.getRoleQualifersForPrincipalByRoleIds(principalId, roleIds,
    // Collections.<String,String>emptyMap());
    //
    // List<SecurityRequestRoleQualification> requestQualifications = new ArrayList<SecurityRequestRoleQualification>();
    // for (Map<String,String> qualification : principalQualifications) {
    // SecurityRequestRoleQualification requestQualification = buildRoleQualificationLine(requestRole, qualification);
    //
    // requestQualifications.add(requestQualification);
    // }
    //
    // requestRole.setRequestRoleQualifications(requestQualifications);
    // // ==== CU Customization (CYNERGY-2376): Added principalQualifications to the method args. ====
    // buildCurrentQualificationsString(requestRole, principalId, principalQualifications);
    // }
    // }
    // else {
    // requestRole.setActive(false);
    // requestRole.setCurrentActive(false);
    // }
    //
    // // if qualified role, setup line for adding new qualification
    // if (requestRole.isQualifiedRole()) {
    // SecurityRequestRoleQualification newRequestRoleQualification = buildRoleQualificationLine(requestRole, null);
    // requestRole.setNewRequestRoleQualification(newRequestRoleQualification);
    // }
    // }
    //
    // /**
    // * Builds a String representation for the principal's qualifications (if any) for the role on the given security request role instance
    // *
    // * @param requestRole
    // * - security request role instance to retrieve qualifications for
    // * @param principalId
    // * - principal id for which the request is being made
    // */
    // // ==== CU Customization (CYNERGY-2376): Updated the string generation to only use the user's direct role memberships. ====
    // protected void buildCurrentQualificationsString(SecurityRequestRole requestRole, String principalId, List<Map<String,String>> principalQualifications) {
    // List<KimTypeAttribute> typeAttributes = KsrUtil.getTypeAttributesForRoleRequest(requestRole);
    //
    // // get all qualifications for user including nested
    // List<String> roleIds = new ArrayList<String>();
    // roleIds.add(requestRole.getRoleId());
    //
    // //List<AttributeSet> allQualifications = KIMServiceLocator.getRoleService().getRoleQualifiersForPrincipalIncludingNested(principalId, roleIds, null);
    // if (principalQualifications == null) {
    // principalQualifications = KimApiServiceLocator.getRoleService().getRoleQualifersForPrincipalByRoleIds(principalId, roleIds, null);
    // }
    //
    // String currentQualificationsString = KsrUtil.buildQualificationString(principalQualifications, typeAttributes);
    // requestRole.setCurrentQualifications(currentQualificationsString);
    // }
    //
    // /**
    // * Builds a <code>SecurityRequestRoleQualification</code> instance for the given security request role and the given qualification set
    // *
    // * @see org.kuali.rice.ksr.service.SecurityRequestDocumentService#buildRoleQualificationLine(org.kuali.rice.ksr.bo.SecurityRequestRole,
    // org.kuali.rice.kim.bo.types.dto.AttributeSet)
    // */
    // @Override
    // public SecurityRequestRoleQualification buildRoleQualificationLine(SecurityRequestRole requestRole, Map<String,String> qualification) {
    // SecurityRequestRoleQualification requestQualification = new SecurityRequestRoleQualification();
    //
    // requestQualification.setDocumentNumber(requestRole.getDocumentNumber());
    // requestQualification.setRoleRequestId(requestRole.getRoleRequestId());
    //
    // KimType typeInfo = KsrUtil.getTypeInfoForRoleRequest(requestRole);
    //
    // List<KimTypeAttribute> typeAttributes = KsrUtil.getTypeAttributesForRoleRequest(requestRole);
    // for (KimTypeAttribute attributeInfo : typeAttributes) {
    // SecurityRequestRoleQualificationDetail requestQualificationDetail = new SecurityRequestRoleQualificationDetail();
    //
    // requestQualificationDetail.setDocumentNumber(requestRole.getDocumentNumber());
    // requestQualificationDetail.setRoleRequestId(requestRole.getRoleRequestId());
    // requestQualificationDetail.setQualificationId(requestRole.getNextQualificationId());
    // requestQualificationDetail.setAttributeId(attributeInfo.getKimAttribute().getId() /*.getKimAttributeId()*/);
    // requestQualificationDetail.setRoleTypeId(typeInfo.getId());
    //
    // if ((qualification != null)
    // && qualification.containsKey(attributeInfo.getKimAttribute().getAttributeName())) {
    // requestQualificationDetail.setAttributeValue(qualification.get(attributeInfo.getKimAttribute().getAttributeName()));
    // }
    //
    // requestQualification.getRoleQualificationDetails().add(requestQualificationDetail);
    // }
    //
    // requestQualification.setQualificationId(requestRole.getNextQualificationId());
    // requestRole.setNextQualificationId(requestRole.getNextQualificationId() + 1);
    //
    // return requestQualification;
    // }

    /**
     * Return all active SecurityGroup objects
     * 
     * @see org.kuali.rice.ksr.service.SecurityRequestDocumentService#getActiveSecurityGroups()
     */
    @Override
    public List<SecurityGroup> getActiveSecurityGroups() {
        Map<String, Object> hashMap = new HashMap<String, Object>();
        hashMap.put("active", true);

        List<SecurityGroup> groupList = (List<SecurityGroup>) businessObjectService.findMatchingOrderBy(SecurityGroup.class, hashMap,
                KSRPropertyConstants.SECURITY_GROUP_NAME, true);

        return groupList;
    }
    
    //
    // protected PersonService getPersonService() {
    // if (personService == null) {
    // personService = KimApiServiceLocator.getPersonService();
    // }
    //
    // return personService;
    // }

    public BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

}
