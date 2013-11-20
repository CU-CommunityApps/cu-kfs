package edu.cornell.kfs.module.purap.document.web.struts;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.module.purap.CUPurapConstants;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurapParameterConstants;
import org.kuali.kfs.module.purap.businessobject.BillingAddress;
import org.kuali.kfs.module.purap.document.RequisitionDocument;
import org.kuali.kfs.module.purap.document.service.PurapService;
import org.kuali.kfs.module.purap.document.service.PurchasingService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.ChartOrgHolder;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.web.struts.FinancialSystemTransactionalDocumentActionBase;
import org.kuali.kfs.sys.service.FinancialSystemUserService;
import org.kuali.kfs.vnd.businessobject.VendorPhoneNumber;
import org.kuali.rice.core.util.KeyLabelPair;
import org.kuali.rice.core.util.RiceConstants;
import org.kuali.rice.kew.exception.WorkflowException;
import org.kuali.rice.kew.util.KEWConstants;
import org.kuali.rice.kim.bo.Person;
import org.kuali.rice.kim.bo.entity.KimEntityEmploymentInformation;
import org.kuali.rice.kim.bo.entity.dto.KimEntityInfo;
import org.kuali.rice.kim.bo.entity.dto.KimPrincipalInfo;
import org.kuali.rice.kim.service.IdentityManagementService;
import org.kuali.rice.kim.service.PersonService;
import org.kuali.rice.kns.bo.Note;
import org.kuali.rice.kns.rule.event.KualiAddLineEvent;
import org.kuali.rice.kns.rule.event.RouteDocumentEvent;
import org.kuali.rice.kns.service.BusinessObjectService;
import org.kuali.rice.kns.service.KualiRuleService;
import org.kuali.rice.kns.service.ParameterService;
import org.kuali.rice.kns.service.PersistenceService;
import org.kuali.rice.kns.util.GlobalVariables;
import org.kuali.rice.kns.util.KNSConstants;
import org.kuali.rice.kns.util.ObjectUtils;
import org.kuali.rice.kns.web.struts.form.KualiDocumentFormBase;
import org.kuali.rice.kns.workflow.service.KualiWorkflowDocument;

import edu.cornell.kfs.module.purap.businessobject.IWantAccount;
import edu.cornell.kfs.module.purap.businessobject.IWantDocUserOptions;
import edu.cornell.kfs.module.purap.businessobject.IWantItem;
import edu.cornell.kfs.module.purap.businessobject.LevelOrganization;
import edu.cornell.kfs.module.purap.document.IWantDocument;
import edu.cornell.kfs.module.purap.document.service.IWantDocumentService;
import edu.cornell.kfs.module.purap.document.validation.event.AddIWantItemEvent;

public class IWantDocumentAction extends FinancialSystemTransactionalDocumentActionBase {

	private static final String IWANT_DEPT_ORGS_TO_EXCLUDE_PARM = "IWANT_DEPT_ORGS_TO_EXCLUDE";
	
	private final int DOCUMENT_DESCRIPTION_MAX_LENGTH = 40;

    /**
     * @see org.kuali.rice.kns.web.struts.action.KualiDocumentActionBase#loadDocument(org.kuali.rice.kns.web.struts.form.KualiDocumentFormBase)
     */
    @Override
    protected void loadDocument(KualiDocumentFormBase kualiDocumentFormBase) throws WorkflowException {

        super.loadDocument(kualiDocumentFormBase);
        IWantDocumentForm iWantForm = (IWantDocumentForm) kualiDocumentFormBase;
        IWantDocument iWantDocument = iWantForm.getIWantDocument();

        if (iWantDocument.getDocumentHeader().getWorkflowDocument().stateIsSaved()) {
            iWantForm.setStep(CUPurapConstants.IWantDocumentSteps.CUSTOMER_DATA_STEP);
            iWantDocument.setStep(CUPurapConstants.IWantDocumentSteps.CUSTOMER_DATA_STEP);
            
            if (StringUtils.isNotBlank(iWantDocument.getCurrentRouteToNetId())) {
                iWantForm.getNewAdHocRoutePerson().setId(iWantDocument.getCurrentRouteToNetId());
            }
        }

        KualiWorkflowDocument workflowDoc = iWantDocument.getDocumentHeader().getWorkflowDocument();
        GlobalVariables.getUserSession().setWorkflowDocument(workflowDoc);

    }

    /**
     * 
     * @see org.kuali.rice.kns.web.struts.action.KualiDocumentActionBase#docHandler(org.apache.struts.action.ActionMapping,
     * org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    @Override
    public ActionForward docHandler(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        ActionForward actionForward = super.docHandler(mapping, form, request, response);
        IWantDocumentForm iWantForm = (IWantDocumentForm) form;
        IWantDocument iWantDocument = iWantForm.getIWantDocument();
        String command = iWantForm.getCommand();
        String step = request.getParameter("step");

        if (step != null) {
            iWantForm.setStep(step);
        }

        if (iWantDocument != null) {
            if (iWantDocument.getDocumentHeader().getWorkflowDocument().stateIsSaved()) {
                step = CUPurapConstants.IWantDocumentSteps.CUSTOMER_DATA_STEP;
            }

            iWantDocument.setStep(step);

            if (KEWConstants.INITIATE_COMMAND.equalsIgnoreCase(command)) {
                IdentityManagementService identityManagementService = SpringContext
                        .getBean(IdentityManagementService.class);

                iWantForm.setDocument(iWantDocument);

                if (iWantDocument != null) {

                    String principalId = iWantDocument.getDocumentHeader().getWorkflowDocument()
                            .getInitiatorPrincipalId();
                    KimPrincipalInfo initiator = identityManagementService.getPrincipal(principalId);
                    String initiatorPrincipalID = initiator.getPrincipalId();
                    String initiatorNetID = initiator.getPrincipalName();

                    iWantDocument.setInitiatorNetID(initiatorNetID);

                    Person currentUser = GlobalVariables.getUserSession().getPerson();
                    String initiatorName = currentUser.getName();
                    String initiatorPhoneNumber = currentUser.getPhoneNumber();
                    String initiatorEmailAddress = currentUser.getEmailAddress();

                    IWantDocumentService iWantDocumentService = SpringContext.getBean(IWantDocumentService.class);
                    String address = iWantDocumentService.getPersonCampusAddress(initiatorNetID);

                    iWantDocument.setInitiatorName(initiatorName);
                    iWantDocument.setInitiatorPhoneNumber(initiatorPhoneNumber);
                    iWantDocument.setInitiatorEmailAddress(initiatorEmailAddress);
                    iWantDocument.setInitiatorAddress(address);

                    // check default user options
                    Map<String, String> primaryKeysCollegeOption = new HashMap<String, String>();
                    primaryKeysCollegeOption.put("principalId", initiatorPrincipalID);
                    primaryKeysCollegeOption.put("optionId", CUPurapConstants.USER_OPTIONS_DEFAULT_COLLEGE);
                    IWantDocUserOptions userOptionsCollege = (IWantDocUserOptions) getBusinessObjectService()
                            .findByPrimaryKey(IWantDocUserOptions.class, primaryKeysCollegeOption);

                    Map<String, String> primaryKeysDepartmentOption = new HashMap<String, String>();
                    primaryKeysDepartmentOption.put("principalId", initiatorPrincipalID);
                    primaryKeysDepartmentOption.put("optionId", CUPurapConstants.USER_OPTIONS_DEFAULT_DEPARTMENT);
                    IWantDocUserOptions userOptionsDepartment = (IWantDocUserOptions) getBusinessObjectService()
                            .findByPrimaryKey(IWantDocUserOptions.class, primaryKeysDepartmentOption);
                    
                    //check default deliver to address info

                    Map<String, String> primaryKeysdeliverToNetIDOption = new HashMap<String, String>();
                    primaryKeysdeliverToNetIDOption.put("principalId", initiatorPrincipalID);
                    primaryKeysdeliverToNetIDOption.put("optionId", CUPurapConstants.USER_OPTIONS_DEFAULT_DELIVER_TO_NET_ID);
                    IWantDocUserOptions userOptionsDeliverToNetID = (IWantDocUserOptions) getBusinessObjectService()
                            .findByPrimaryKey(IWantDocUserOptions.class, primaryKeysdeliverToNetIDOption);

                    Map<String, String> primaryKeysDeliverToNameOption = new HashMap<String, String>();
                    primaryKeysDeliverToNameOption.put("principalId", initiatorPrincipalID);
                    primaryKeysDeliverToNameOption.put("optionId", CUPurapConstants.USER_OPTIONS_DEFAULT_DELIVER_TO_NAME);
                    IWantDocUserOptions userOptionsDeliverToName = (IWantDocUserOptions) getBusinessObjectService()
                            .findByPrimaryKey(IWantDocUserOptions.class, primaryKeysDeliverToNameOption);
                    
                    Map<String, String> primaryKeysDeliverToEmailOption = new HashMap<String, String>();
                    primaryKeysDeliverToEmailOption.put("principalId", initiatorPrincipalID);
                    primaryKeysDeliverToEmailOption.put("optionId", CUPurapConstants.USER_OPTIONS_DEFAULT_DELIVER_TO_EMAIL_ADDRESS);
                    IWantDocUserOptions userOptionsDeliverToEmail = (IWantDocUserOptions) getBusinessObjectService()
                            .findByPrimaryKey(IWantDocUserOptions.class, primaryKeysDeliverToEmailOption);
                    
                    Map<String, String> primaryKeysDeliverToPhnNbrOption = new HashMap<String, String>();
                    primaryKeysDeliverToPhnNbrOption.put("principalId", initiatorPrincipalID);
                    primaryKeysDeliverToPhnNbrOption.put("optionId", CUPurapConstants.USER_OPTIONS_DEFAULT_DELIVER_TO_PHONE_NUMBER);
                    IWantDocUserOptions userOptionsDeliverToPhnNbr = (IWantDocUserOptions) getBusinessObjectService()
                            .findByPrimaryKey(IWantDocUserOptions.class, primaryKeysDeliverToPhnNbrOption);
                    
                    Map<String, String> primaryKeysDeliverToAddressOption = new HashMap<String, String>();
                    primaryKeysDeliverToAddressOption.put("principalId", initiatorPrincipalID);
                    primaryKeysDeliverToAddressOption.put("optionId", CUPurapConstants.USER_OPTIONS_DEFAULT_DELIVER_TO_ADDRESS);
                    IWantDocUserOptions userOptionsDeliverToAddress = (IWantDocUserOptions) getBusinessObjectService()
                            .findByPrimaryKey(IWantDocUserOptions.class, primaryKeysDeliverToAddressOption);

                    if (ObjectUtils.isNotNull(userOptionsCollege)) {
                        iWantDocument.setCollegeLevelOrganization(userOptionsCollege.getOptionValue());
                    }

                    if (ObjectUtils.isNotNull(userOptionsDepartment)) {
                        iWantDocument.setDepartmentLevelOrganization(userOptionsDepartment.getOptionValue());
                    }

                    //if no default user options check primary department
                    if (ObjectUtils.isNull(userOptionsCollege) && ObjectUtils.isNull(userOptionsDepartment)) {

                        /// set college and department based on primary id
                        setCollegeAndDepartmentBasedOnPrimaryDepartment(iWantForm);
                    }
                    
                    if (ObjectUtils.isNotNull(userOptionsDeliverToNetID)) {
                        iWantDocument.setDeliverToNetID(userOptionsDeliverToNetID.getOptionValue());
                    }
                    
                    if (ObjectUtils.isNotNull(userOptionsDeliverToName)) {
                        iWantDocument.setDeliverToName(userOptionsDeliverToName.getOptionValue());
                    }
                    
                    if (ObjectUtils.isNotNull(userOptionsDeliverToEmail)) {
                        iWantDocument.setDeliverToEmailAddress(userOptionsDeliverToEmail.getOptionValue());
                    }
                    
                    if (ObjectUtils.isNotNull(userOptionsDeliverToPhnNbr)) {
                        iWantDocument.setDeliverToPhoneNumber(userOptionsDeliverToPhnNbr.getOptionValue());
                    }
                    
                    if (ObjectUtils.isNotNull(userOptionsDeliverToAddress)) {
                        iWantDocument.setDeliverToAddress(userOptionsDeliverToAddress.getOptionValue());
                    }
                }

                // put workflow doc on session

                KualiWorkflowDocument workflowDoc = iWantDocument.getDocumentHeader().getWorkflowDocument();
                // KualiDocumentFormBase.populate() needs this updated in the session
                GlobalVariables.getUserSession().setWorkflowDocument(workflowDoc);

                setIWantDocumentDescription(iWantDocument);
            }
        }

        return actionForward;
    }

    /**
     * 
     * @see org.kuali.rice.kns.web.struts.action.KualiAction#execute(org.apache.struts.action.ActionMapping,
     * org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        ActionForward actionForward = super.execute(mapping, form, request, response);
        IWantDocumentForm documentForm = (IWantDocumentForm) form;
        IWantDocument iWantDoc = documentForm.getIWantDocument();

        if (documentForm != null && documentForm.getDocument() != null) {

            iWantDoc.setExplanation(iWantDoc.getDocumentHeader().getExplanation());

            if (!documentForm.getPreviousSelectedOrg().equalsIgnoreCase(
                    ((IWantDocument) documentForm.getDocument()).getCollegeLevelOrganization())) {

                // populate department drop down
                if (documentForm != null
                        && documentForm.getDocument() != null) {

                    String cLevelOrg = ((IWantDocument) documentForm.getDocument()).getCollegeLevelOrganization();

                    documentForm.getDeptOrgKeyLabels().clear();
                    documentForm.getDeptOrgKeyLabels().add(new KeyLabelPair("", "Please Select"));

                    if (StringUtils.isNotEmpty(cLevelOrg)) {

                        IWantDocumentService iWantDocumentService = SpringContext.getBean(IWantDocumentService.class);
                        List<LevelOrganization> dLevelOrgs = iWantDocumentService.getDLevelOrganizations(cLevelOrg);

                        // Get the list of chart+org combos to forcibly exclude from the drop-down, if any.
                        String routingChart = ((IWantDocument) documentForm.getDocument()).getRoutingChart();
                        List<String> dLevelExcludesList = getParameterService().getParameterValues(PurapConstants.PURAP_NAMESPACE,
                        		KNSConstants.DetailTypes.DOCUMENT_DETAIL_TYPE, IWANT_DEPT_ORGS_TO_EXCLUDE_PARM);
                        Set<String> dLevelExcludes =
                        		new HashSet<String>((dLevelExcludesList != null) ? dLevelExcludesList : Collections.<String>emptyList());
                        
                        for (LevelOrganization levelOrganization : dLevelOrgs) {

                        	// Add each department-level org to the drop-down as long as it is not marked for exclusion.
                        	if (!dLevelExcludes.contains(routingChart + "=" + levelOrganization.getCode())) {
                        		documentForm.getDeptOrgKeyLabels()
                                    	.add(
                                    			new KeyLabelPair(levelOrganization.getCode(), levelOrganization
                                    					.getCodeAndDescription()));
                        	}
                        }
                    }
                }
            }

            //setIWantDocumentDescription(iWantDoc);

        }

        return actionForward;
    }

    private void setIWantDocumentDescription(IWantDocument iWantDocument) {
        // add selected chart and department to document description
        String routingChart = iWantDocument.getRoutingChart() == null ? StringUtils.EMPTY : iWantDocument
                .getRoutingChart() + "-";
        String routingOrg = iWantDocument.getRoutingOrganization() == null ? StringUtils.EMPTY : iWantDocument
                .getRoutingOrganization();
        String addChartOrgToDesc = routingChart + routingOrg;
        String vendorName = iWantDocument.getVendorName() == null ? StringUtils.EMPTY : iWantDocument.getVendorName();
        String description = addChartOrgToDesc + " " + vendorName;

        if (StringUtils.isNotBlank(description) && description.length() > DOCUMENT_DESCRIPTION_MAX_LENGTH) {
            description = description.substring(0, DOCUMENT_DESCRIPTION_MAX_LENGTH);
        }

        // If necessary, add a default description.
        if (StringUtils.isBlank(description)) {
        	description = "New IWantDocument";
        }
        
        iWantDocument.getDocumentHeader().setDocumentDescription(description);
    }

    /**
     * Sets the College and Department based on the initiator primary department.
     * 
     * @param documentForm
     */
    private void setCollegeAndDepartmentBasedOnPrimaryDepartment(IWantDocumentForm documentForm) {
        IWantDocumentService iWantDocumentService = SpringContext.getBean(IWantDocumentService.class);
        String primaryDeptOrg = null;

        if (documentForm != null && documentForm.getDocument() != null
                && StringUtils.isEmpty(((IWantDocument) documentForm.getDocument()).getCollegeLevelOrganization())) {

            Person currentUser = GlobalVariables.getUserSession().getPerson();
            IdentityManagementService identityManagementService = SpringContext
                    .getBean(IdentityManagementService.class);

            KimEntityInfo entityInfo = identityManagementService.getEntityInfoByPrincipalId(currentUser
                    .getPrincipalId());

            if (ObjectUtils.isNotNull(entityInfo)) {
                if (ObjectUtils.isNotNull(entityInfo.getEmploymentInformation())
                        && entityInfo.getEmploymentInformation().size() > 0) {
                    KimEntityEmploymentInformation employmentInformation = entityInfo.getEmploymentInformation().get(0);
                    String primaryDepartment = employmentInformation.getPrimaryDepartmentCode();
                    primaryDeptOrg = primaryDepartment.substring(primaryDepartment.lastIndexOf("-") + 1,
                            primaryDepartment.length());

                    String cLevelOrg = iWantDocumentService.getCLevelOrganizationForDLevelOrg(primaryDepartment);
                    ((IWantDocument) documentForm.getDocument()).setCollegeLevelOrganization(cLevelOrg);
                }
            }
        }

        if (documentForm != null && documentForm.getDocument() != null
                && StringUtils.isNotEmpty(((IWantDocument) documentForm.getDocument()).getCollegeLevelOrganization())) {
            String cLevelOrg = ((IWantDocument) documentForm.getDocument()).getCollegeLevelOrganization();
            documentForm.getDeptOrgKeyLabels().clear();
            documentForm.getDeptOrgKeyLabels().add(new KeyLabelPair("", "Please Select"));

            List<LevelOrganization> dLevelOrgs = iWantDocumentService.getDLevelOrganizations(cLevelOrg);

            for (LevelOrganization levelOrganization : dLevelOrgs) {
                documentForm.getDeptOrgKeyLabels().add(
                        new KeyLabelPair(levelOrganization.getCode(), levelOrganization.getCodeAndDescription()));

            }

            if (primaryDeptOrg != null) {
                ((IWantDocument) documentForm.getDocument()).setDepartmentLevelOrganization(primaryDeptOrg);
            }

        }
    }

    /**
     * Loads the D level orgs based on the selected C level org
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ActionForward loadDLevelOrgs(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        return mapping.findForward("refresh");
    }

    /**
     * Takes the user to next page
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ActionForward continueToCustomerData(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        IWantDocumentForm iWantForm = (IWantDocumentForm) form;
        IWantDocument iWantDocument = iWantForm.getIWantDocument();

        iWantForm.setStep(CUPurapConstants.IWantDocumentSteps.CUSTOMER_DATA_STEP);

        iWantDocument.setStep(CUPurapConstants.IWantDocumentSteps.CUSTOMER_DATA_STEP);

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * Takes the user to next page
     * 
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ActionForward continueToItems(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        IWantDocumentForm iWantForm = (IWantDocumentForm) form;
        IWantDocument iWantDocument = iWantForm.getIWantDocument();

        iWantDocument.setExplanation(iWantDocument.getDocumentHeader().getExplanation());

        KualiRuleService ruleService = SpringContext.getBean(KualiRuleService.class);
        boolean rulePassed = true;

        // call business rules
        rulePassed &= ruleService
                .applyRules(new RouteDocumentEvent(KFSConstants.DOCUMENT_HEADER_ERRORS, iWantDocument));

        if (rulePassed) {
            iWantForm.setStep(CUPurapConstants.IWantDocumentSteps.ITEMS_AND_ACCT_DATA_STEP);

            iWantDocument.setStep(CUPurapConstants.IWantDocumentSteps.ITEMS_AND_ACCT_DATA_STEP);
        }

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * Takes the user to next page
     * 
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ActionForward continueToVendor(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        IWantDocumentForm iWantForm = (IWantDocumentForm) form;
        IWantDocument iWantDocument = iWantForm.getIWantDocument();
        
        //add new item and new accounting line if not empty
        IWantItem item = iWantForm.getNewIWantItemLine();
        if(StringUtils.isNotBlank(item.getItemDescription()) || item.getItemUnitPrice() != null || item.getItemQuantity()!= null){
            boolean added = addNewItem(iWantForm, iWantDocument, item);
            
            if(!added){
                return mapping.findForward(RiceConstants.MAPPING_BASIC);
            }
        }
        
        IWantAccount account = iWantForm.getNewSourceLine();
        if(StringUtils.isNotBlank(account.getAccountNumber()) || StringUtils.isNotBlank(account.getSubAccountNumber()) || StringUtils.isNotBlank(account.getFinancialObjectCode()) || StringUtils.isNotBlank(account.getFinancialSubObjectCode()) || StringUtils.isNotBlank(account.getProjectCode()) || StringUtils.isNotBlank(account.getOrganizationReferenceId())){
            boolean added = addNewAccount(iWantForm, iWantDocument, account);
            
            if(!added){
                return mapping.findForward(RiceConstants.MAPPING_BASIC);
            }
        }

        KualiRuleService ruleService = SpringContext.getBean(KualiRuleService.class);
        boolean rulePassed = true;

        // call business rules
        rulePassed &= ruleService.applyRules(new RouteDocumentEvent("", iWantDocument));

        if (rulePassed) {
            iWantForm.setStep(CUPurapConstants.IWantDocumentSteps.VENDOR_STEP);

            iWantDocument.setStep(CUPurapConstants.IWantDocumentSteps.VENDOR_STEP);
        }

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * Takes the user to next page
     * 
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ActionForward continueToRouting(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {

    	IWantDocumentForm iWantForm = (IWantDocumentForm) form;
        IWantDocument iWantDocument = iWantForm.getIWantDocument();

        // call business rules
        KualiRuleService ruleService = SpringContext.getBean(KualiRuleService.class);
        boolean rulePassed = true;
        rulePassed &= ruleService.applyRules(new RouteDocumentEvent("", iWantDocument));

        if (rulePassed) {
        	iWantForm.setStep(CUPurapConstants.IWantDocumentSteps.ROUTING_STEP);
        	iWantDocument.setStep(CUPurapConstants.IWantDocumentSteps.ROUTING_STEP);
        }

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * @see org.kuali.rice.kns.web.struts.action.KualiDocumentActionBase#close(org.apache.struts.action.ActionMapping,
     * org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    public ActionForward close(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        super.close(mapping, form, request, response);

        return mapping.findForward(KFSConstants.MAPPING_PORTAL);
    }

    /**
     * Adds an item to the document
     * 
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ActionForward addItem(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        
        IWantDocumentForm iWantDocumentForm = (IWantDocumentForm) form;
        IWantDocument iWantDocument = (IWantDocument) iWantDocumentForm.getDocument();
        IWantItem item = iWantDocumentForm.getNewIWantItemLine();
        
        addNewItem(iWantDocumentForm, iWantDocument, item);
        
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * Deletes the selected item
     * 
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ActionForward deleteItem(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        IWantDocumentForm iWantForm = (IWantDocumentForm) form;

        IWantDocument iWantDocument = (IWantDocument) iWantForm.getDocument();
        iWantDocument.deleteItem(getSelectedLine(request));

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * Retrieves the selected line.
     * 
     * @see org.kuali.rice.kns.web.struts.action.KualiAction#getSelectedLine(javax.servlet.http.HttpServletRequest)
     */
    protected int getSelectedLine(HttpServletRequest request) {
        int selectedLine = -1;
        String parameterName = (String) request.getAttribute(KNSConstants.METHOD_TO_CALL_ATTRIBUTE);
        if (StringUtils.isNotBlank(parameterName)) {
            String lineNumber = StringUtils.substringBetween(parameterName, ".line", ".");
            selectedLine = Integer.parseInt(lineNumber);
        }

        return selectedLine;
    }

    /**
     * Adds an accounting line
     * 
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ActionForward addAccountingLine(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        IWantDocumentForm iWantDocumentForm = (IWantDocumentForm) form;
        IWantDocument iWantDoc = (IWantDocument) iWantDocumentForm.getDocument();
        IWantAccount account = iWantDocumentForm.getNewSourceLine();
        
        addNewAccount(iWantDocumentForm, iWantDoc, account);
        
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * Deletes the selected account
     * 
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ActionForward deleteAccount(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        IWantDocumentForm iWantForm = (IWantDocumentForm) form;

        IWantDocument iWantDocument = (IWantDocument) iWantForm.getDocument();
        iWantDocument.deleteAccount(getSelectedLine(request));

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * 
     * @see org.kuali.rice.kns.web.struts.action.KualiDocumentActionBase#route(org.apache.struts.action.ActionMapping,
     * org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    @Override
    public ActionForward route(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        IWantDocumentForm iWantDocForm = (IWantDocumentForm) form;
        IWantDocument iWantDocument = iWantDocForm.getIWantDocument();
        
        //add new item and new accounting line if not empty
        IWantItem item = iWantDocForm.getNewIWantItemLine();
        if(StringUtils.isNotBlank(item.getItemDescription()) || item.getItemUnitPrice() != null || item.getItemQuantity()!= null){
            boolean added = addNewItem(iWantDocForm, iWantDocument, item);
            
            if(!added){
                return mapping.findForward(RiceConstants.MAPPING_BASIC);
            }
        }
        
        IWantAccount account = iWantDocForm.getNewSourceLine();
        if(StringUtils.isNotBlank(account.getAccountNumber()) || StringUtils.isNotBlank(account.getSubAccountNumber()) || StringUtils.isNotBlank(account.getFinancialObjectCode()) || StringUtils.isNotBlank(account.getFinancialSubObjectCode()) || StringUtils.isNotBlank(account.getProjectCode()) || StringUtils.isNotBlank(account.getOrganizationReferenceId())){
            boolean added = addNewAccount(iWantDocForm, iWantDocument, account);
            
            if(!added){
                return mapping.findForward(RiceConstants.MAPPING_BASIC);
            }
        }  

        iWantDocument.setExplanation(iWantDocument.getDocumentHeader().getExplanation());

        String step = iWantDocForm.getStep();
        String principalId = iWantDocument.getDocumentHeader().getWorkflowDocument().getInitiatorPrincipalId();

        boolean setDefaultCollegeDept = iWantDocument.isUseCollegeAndDepartmentAsDefault();

        if (setDefaultCollegeDept) {
            // set these values in user Options table
            String college = iWantDocument.getCollegeLevelOrganization();
            String department = iWantDocument.getDepartmentLevelOrganization();
            
            
            saveUserOption(principalId, CUPurapConstants.USER_OPTIONS_DEFAULT_COLLEGE, college);

            saveUserOption(principalId, CUPurapConstants.USER_OPTIONS_DEFAULT_DEPARTMENT, department);

        }
        
        if(iWantDocument.isSetDeliverToInfoAsDefault()){
            
            if(StringUtils.isNotBlank(iWantDocument.getDeliverToNetID())){
                saveUserOption(principalId, CUPurapConstants.USER_OPTIONS_DEFAULT_DELIVER_TO_NET_ID, iWantDocument.getDeliverToNetID());
            }
            
            if(StringUtils.isNotBlank(iWantDocument.getDeliverToName())){
                saveUserOption(principalId, CUPurapConstants.USER_OPTIONS_DEFAULT_DELIVER_TO_NAME, iWantDocument.getDeliverToName());
            }
            
            if(StringUtils.isNotBlank(iWantDocument.getDeliverToEmailAddress())){
                saveUserOption(principalId, CUPurapConstants.USER_OPTIONS_DEFAULT_DELIVER_TO_EMAIL_ADDRESS, iWantDocument.getDeliverToEmailAddress());
            }
            
            if(StringUtils.isNotBlank(iWantDocument.getDeliverToPhoneNumber())){
                saveUserOption(principalId, CUPurapConstants.USER_OPTIONS_DEFAULT_DELIVER_TO_PHONE_NUMBER, iWantDocument.getDeliverToPhoneNumber());
            }
            
            if(StringUtils.isNotBlank(iWantDocument.getDeliverToAddress())){
                saveUserOption(principalId, CUPurapConstants.USER_OPTIONS_DEFAULT_DELIVER_TO_ADDRESS, iWantDocument.getDeliverToAddress());
            }
            
        }

        setIWantDocumentDescription(iWantDocument);

        //insert adhoc route person first and the route
        if (StringUtils.isNotBlank(iWantDocForm.getNewAdHocRoutePerson().getId())) {
            iWantDocument.setCurrentRouteToNetId(iWantDocForm.getNewAdHocRoutePerson().getId());
            insertAdHocRoutePerson(mapping, iWantDocForm, request, response);

        }
        ActionForward actionForward = super.route(mapping, form, request, response);

        if (CUPurapConstants.IWantDocumentSteps.ROUTING_STEP.equalsIgnoreCase(step)) {
            return mapping.findForward("finish");
        } else {
            return actionForward;
        }
    }
    
    private void saveUserOption(String principalId, String userOptionName, String userOptionValue){
        IWantDocUserOptions userOption = new IWantDocUserOptions();
        userOption.setPrincipalId(principalId);
        userOption.setOptionId(userOptionName);
        userOption.setOptionValue(userOptionValue);

        IWantDocUserOptions retrievedUserOption = (IWantDocUserOptions) getBusinessObjectService()
                .retrieve(userOption);

        if (ObjectUtils.isNotNull(retrievedUserOption)) {
            retrievedUserOption.setOptionValue(userOptionValue);
            getBusinessObjectService().save(retrievedUserOption);
        } else {
            getBusinessObjectService().save(userOption);
        }
    }

    @Override
    public ActionForward sendAdHocRequests(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        IWantDocumentForm iWantDocForm = (IWantDocumentForm) form;
        IWantDocument iWantDocument = iWantDocForm.getIWantDocument();

        //insert adhoc route person first and the route
        if (StringUtils.isNotBlank(iWantDocForm.getNewAdHocRoutePerson().getId())) {
            insertAdHocRoutePerson(mapping, iWantDocForm, request, response);

        }
        return super.sendAdHocRequests(mapping, form, request, response);
    }

    @Override
	public ActionForward approve(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
    	IWantDocumentForm iWantDocForm = (IWantDocumentForm) form;
        IWantDocument iWantDocument = iWantDocForm.getIWantDocument();

        //insert adhoc route person first and then approve
        if (StringUtils.isNotBlank(iWantDocForm.getNewAdHocRoutePerson().getId())) {
            insertAdHocRoutePerson(mapping, iWantDocForm, request, response);

        }
    	
		return super.approve(mapping, form, request, response);
	}
    
    /**
     * Use the new attachment description field to set the note text.
     * 
     * @see org.kuali.rice.kns.web.struts.action.KualiDocumentActionBase#insertBONote(org.apache.struts.action.ActionMapping,
     * org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    @Override
    public ActionForward insertBONote(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {

    	// If the note text is blank, set the attachment description as the text. Otherwise, concatenate both to form the text.
    	IWantDocumentForm iWantDocumentForm = (IWantDocumentForm) form;
    	Note note = iWantDocumentForm.getNewNote();
    	if (StringUtils.isBlank(note.getNoteText())) {
            note.setNoteText(iWantDocumentForm.getIWantDocument().getAttachmentDescription());
        } else {
        	note.setNoteText(iWantDocumentForm.getIWantDocument().getAttachmentDescription() + ": " + note.getNoteText());
        }
    	
        ActionForward actionForward = super.insertBONote(mapping, form, request, response);
        
        iWantDocumentForm.getIWantDocument().setAttachmentDescription(StringUtils.EMPTY);

        return actionForward;
    }

    /**
     * Overridden to guarantee that form of copied document is set to whatever the entry
     * mode of the document is
     * @see org.kuali.rice.kns.web.struts.action.KualiTransactionalDocumentActionBase#copy(org.apache.struts.action.ActionMapping,
     * org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    @Override
    public ActionForward copy(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        ActionForward forward = null;

        if (request.getParameter("docId") == null) {
            forward = super.copy(mapping, form, request, response);
        } else {
            // this is copy document from Procurement Gateway:
            // use this link to call: http://localhost:8080/kfs-dev/purapIWant.do?methodToCall=copy&docId=xxxx
            String docId = request.getParameter("docId");
            KualiDocumentFormBase kualiDocumentFormBase = (KualiDocumentFormBase) form;

            IWantDocument document = null;
            document = (IWantDocument) getDocumentService().getByDocumentHeaderId(docId);
            document.toCopyFromGateway();

            kualiDocumentFormBase.setDocument(document);
            KualiWorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
            kualiDocumentFormBase.setDocTypeName(workflowDocument.getDocumentType());
            GlobalVariables.getUserSession().setWorkflowDocument(workflowDocument);

            forward = mapping.findForward(RiceConstants.MAPPING_BASIC);
        }
        return forward;
    }

    /**
     * 
     * @see org.kuali.rice.kns.web.struts.action.KualiDocumentActionBase#refresh(org.apache.struts.action.ActionMapping,
     * org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    @Override
    public ActionForward refresh(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        IWantDocumentForm iWantDocForm = (IWantDocumentForm) form;
        IWantDocument iWantDocument = iWantDocForm.getIWantDocument();

        ActionForward actionForward = super.refresh(mapping, form, request, response);
        String refreshCaller = request.getParameter(KFSConstants.REFRESH_CALLER);

        if (refreshCaller != null && refreshCaller.endsWith(KFSConstants.LOOKUPABLE_SUFFIX)) {

            String deliverToNetID = iWantDocument.getDeliverToNetID();
            if (StringUtils.isNotEmpty(deliverToNetID)) {
                IWantDocumentService iWantDocumentService = SpringContext.getBean(IWantDocumentService.class);
                String address = iWantDocumentService.getPersonCampusAddress(deliverToNetID);

                iWantDocument.setDeliverToAddress(address);

            }

            if ("iWantDocVendorLookupable".equalsIgnoreCase(refreshCaller)) {
                Integer vendorHeaderId = iWantDocument.getVendorHeaderGeneratedIdentifier();
                Integer vendorId = iWantDocument.getVendorDetailAssignedIdentifier();
                String phoneNumber = "Phone: ";

                Map fieldValues = new HashMap();
                fieldValues.put("vendorHeaderGeneratedIdentifier", vendorHeaderId);
                fieldValues.put("vendorDetailAssignedIdentifier", vendorId);
                fieldValues.put("vendorPhoneTypeCode", "PH");
                Collection vendorPhoneNumbers = getBusinessObjectService().findMatching(VendorPhoneNumber.class,
                        fieldValues);
                if (ObjectUtils.isNotNull(vendorPhoneNumbers) && vendorPhoneNumbers.size() > 0) {
                    VendorPhoneNumber retrievedVendorPhoneNumber = (VendorPhoneNumber) vendorPhoneNumbers.toArray()[0];
                    phoneNumber += retrievedVendorPhoneNumber.getVendorPhoneNumber();
                }

                // populate vendor info
                String addressLine1 = iWantDocument.getVendorLine1Address() != null ? iWantDocument
                        .getVendorLine1Address() : StringUtils.EMPTY;
                String addressLine2 = iWantDocument.getVendorLine2Address() != null ? iWantDocument
                        .getVendorLine2Address() : StringUtils.EMPTY;
                String cityName = iWantDocument.getVendorCityName() != null ? iWantDocument.getVendorCityName()
                        : StringUtils.EMPTY;
                String stateCode = iWantDocument.getVendorStateCode() != null ? iWantDocument.getVendorStateCode()
                        : StringUtils.EMPTY;
                String countryCode = iWantDocument.getVendorCountryCode() != null ? iWantDocument
                        .getVendorCountryCode() : StringUtils.EMPTY;
                String postalCode = iWantDocument.getVendorPostalCode() != null ? iWantDocument.getVendorPostalCode()
                        : StringUtils.EMPTY;
                String faxNumber = "Fax: "
                        + (iWantDocument.getVendorFaxNumber() != null ? iWantDocument.getVendorFaxNumber()
                                : StringUtils.EMPTY);

                String URL = "URL: "
                        + (iWantDocument.getVendorWebURL() != null ? iWantDocument.getVendorWebURL()
                                : StringUtils.EMPTY);

                String vendorInfo = addressLine1 + "\n"
                            + addressLine2 + "\n"
                            + cityName + ", " + postalCode + ", " + stateCode + ", " + countryCode + "\n"
                            + faxNumber + "\n"
                            + phoneNumber + " \n"
                            + URL;

                iWantDocument.setVendorDescription(vendorInfo);
            }

        }
        return actionForward;
    }

    @Override
    public ActionForward save(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {

    	// Only recreate document description if in INITIATED or SAVED status.
    	KualiWorkflowDocument workflowDocument = ((KualiDocumentFormBase)form).getDocument().getDocumentHeader().getWorkflowDocument();
    	if (workflowDocument.stateIsInitiated() || workflowDocument.stateIsSaved()) {
    		setIWantDocumentDescription( (IWantDocument) ((KualiDocumentFormBase)form).getDocument() );
    	}

        ActionForward actionForward = super.save(mapping, form, request, response);
        IWantDocumentForm iWantDocForm = (IWantDocumentForm) form;
        IWantDocument iWantDocument = iWantDocForm.getIWantDocument();
        
        //add new item and new accounting line if not empty
        IWantItem item = iWantDocForm.getNewIWantItemLine();
        if(StringUtils.isNotBlank(item.getItemDescription()) || item.getItemUnitPrice() != null || item.getItemQuantity()!= null){
            boolean added = addNewItem(iWantDocForm, iWantDocument, item);
            
            if(!added){
                return mapping.findForward(RiceConstants.MAPPING_BASIC);
            }
        }
        
        IWantAccount account = iWantDocForm.getNewSourceLine();
        if(StringUtils.isNotBlank(account.getAccountNumber()) || StringUtils.isNotBlank(account.getSubAccountNumber()) || StringUtils.isNotBlank(account.getFinancialObjectCode()) || StringUtils.isNotBlank(account.getFinancialSubObjectCode()) || StringUtils.isNotBlank(account.getProjectCode()) || StringUtils.isNotBlank(account.getOrganizationReferenceId())){
            boolean added = addNewAccount(iWantDocForm, iWantDocument, account);
            
            if(!added){
                return mapping.findForward(RiceConstants.MAPPING_BASIC);
            }
        }

        iWantDocument.setExplanation(iWantDocument.getDocumentHeader().getExplanation());

        if (StringUtils.isNotBlank(iWantDocForm.getNewAdHocRoutePerson().getId())) {
            iWantDocument.setCurrentRouteToNetId(iWantDocForm.getNewAdHocRoutePerson().getId());
        }

        return actionForward;
    }
    
    private boolean addNewAccount(IWantDocumentForm iWantDocumentForm, IWantDocument iWantDoc, IWantAccount account){

        KualiRuleService ruleService = SpringContext.getBean(KualiRuleService.class);
        boolean acctRulesPassed = true;

        acctRulesPassed &= ruleService.applyRules(new KualiAddLineEvent(iWantDoc, "accounts", account));

        if (acctRulesPassed) {
            account = iWantDocumentForm.getAndResetNewIWantAccountLine();
            iWantDoc.addAccount(account);
        }  
        
        return acctRulesPassed;
        
    }
    
    private boolean addNewItem(IWantDocumentForm iWantDocumentForm, IWantDocument iWantDoc, IWantItem item){

        KualiRuleService ruleService = SpringContext.getBean(KualiRuleService.class);
        boolean rulePassed = true;

        // call business rules
        rulePassed &= ruleService.applyRules(new AddIWantItemEvent(StringUtils.EMPTY, iWantDoc, item));

        if (rulePassed) {
            item = iWantDocumentForm.getAndResetNewIWantItemLine();
            iWantDoc.addItem(item);
        }
        
        return rulePassed;
    }

    public ActionForward createRequisition(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        
        IWantDocumentForm iWantDocForm = (IWantDocumentForm) form;
        IWantDocument iWantDocument = iWantDocForm.getIWantDocument();
        IWantDocumentService iWantDocumentService = SpringContext.getBean(IWantDocumentService.class);
        
       // RequisitionDocument requisitionDocument = iWantDocumentService.createRequisition(iWantDocument);
        
       // getDocumentService().saveDocument(requisitionDocument);
        
       // requisitionDocument.getUrl();
        
        String url = "/purapRequisition.do?methodToCall=docHandler&command=initiate&docTypeName=REQS";
        
//        KualiWorkflowDocument workflowDoc = requisitionDocument.getDocumentHeader().getWorkflowDocument();
//        GlobalVariables.getUserSession().setWorkflowDocument(workflowDoc);
//        
        ActionForward actionForward = new ActionForward(url, true);
   
//        request.getParameterMap().put("methodToCall", "docHandler");
//       
//        request.getParameterMap().put("command", "initiate");
//        request.getParameterMap().put("docType", "REQS");
        
        return actionForward;//mapping.findForward("requisition");
    }
    
    private RequisitionDocument createNewRequisition(IWantDocument iWantDocument) throws WorkflowException{
        
        RequisitionDocument requisitionDocument = (RequisitionDocument)getDocumentService().getNewDocument(RequisitionDocument.class);
        
        requisitionDocument.getDocumentHeader().setDocumentDescription(iWantDocument.getDepartmentLevelOrganization() + ", "+ iWantDocument.getInitiatorName() + ", " +"I Want doc #" + iWantDocument.getDocumentNumber());
        requisitionDocument.setRequisitionSourceCode(PurapConstants.RequisitionSources.IWNT);
        requisitionDocument.setStatusCode(PurapConstants.RequisitionStatuses.IN_PROCESS);
        
        requisitionDocument.setPurchaseOrderCostSourceCode(PurapConstants.POCostSources.ESTIMATE);
        requisitionDocument.setPurchaseOrderTransmissionMethodCode(SpringContext.getBean(ParameterService.class).getParameterValue(RequisitionDocument.class, PurapParameterConstants.PURAP_DEFAULT_PO_TRANSMISSION_CODE));
        requisitionDocument.setDocumentFundingSourceCode(SpringContext.getBean(ParameterService.class).getParameterValue(RequisitionDocument.class, PurapParameterConstants.DEFAULT_FUNDING_SOURCE));
        requisitionDocument.setUseTaxIndicator(SpringContext.getBean(PurchasingService.class).getDefaultUseTaxIndicatorValue(requisitionDocument));
        
        if(StringUtils.isNotBlank(iWantDocument.getDeliverToNetID())){
        Person deliverTo = SpringContext.getBean(PersonService.class).getPersonByPrincipalName(iWantDocument.getDeliverToNetID());
        
        Person currentUser = GlobalVariables.getUserSession().getPerson();
        ChartOrgHolder purapChartOrg = SpringContext.getBean(FinancialSystemUserService.class).getPrimaryOrganization(currentUser, PurapConstants.PURAP_NAMESPACE);
        if (ObjectUtils.isNotNull(purapChartOrg)) {
            requisitionDocument.setChartOfAccountsCode(purapChartOrg.getChartOfAccountsCode());
            requisitionDocument.setOrganizationCode(purapChartOrg.getOrganizationCode());
        }
        requisitionDocument.setDeliveryCampusCode(deliverTo.getCampusCode());
        requisitionDocument.setDeliveryToName(iWantDocument.getDeliverToName());
        requisitionDocument.setDeliveryToEmailAddress(iWantDocument.getDeliverToEmailAddress());
        requisitionDocument.setDeliveryToPhoneNumber(iWantDocument.getDeliverToPhoneNumber());
        requisitionDocument.setRequestorPersonName(iWantDocument.getInitiatorName());
        requisitionDocument.setRequestorPersonEmailAddress(iWantDocument.getInitiatorEmailAddress());
        requisitionDocument.setRequestorPersonPhoneNumber(iWantDocument.getInitiatorPhoneNumber());
        
        
        requisitionDocument.setOrganizationAutomaticPurchaseOrderLimit(SpringContext.getBean(PurapService.class).getApoLimit(requisitionDocument.getVendorContractGeneratedIdentifier(), requisitionDocument.getChartOfAccountsCode(), requisitionDocument.getOrganizationCode()));

        // populate billing address
        BillingAddress billingAddress = new BillingAddress();
        billingAddress.setBillingCampusCode(requisitionDocument.getDeliveryCampusCode());
        Map keys = SpringContext.getBean(PersistenceService.class).getPrimaryKeyFieldValues(billingAddress);
        billingAddress = (BillingAddress) SpringContext.getBean(BusinessObjectService.class).findByPrimaryKey(BillingAddress.class, keys);
        requisitionDocument.templateBillingAddress(billingAddress);
        }
        
        KualiWorkflowDocument workflowDoc = requisitionDocument.getDocumentHeader().getWorkflowDocument();
        GlobalVariables.getUserSession().setWorkflowDocument(workflowDoc);
        
       
        return requisitionDocument;
    }

}
