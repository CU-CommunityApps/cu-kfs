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

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.core.api.config.property.ConfigContext;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.api.util.ConcreteKeyValue;
import org.kuali.kfs.kew.api.KewApiConstants;
import org.kuali.kfs.kew.api.WorkflowDocument;
import org.kuali.kfs.kim.api.services.KimApiServiceLocator;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.kim.impl.identity.employment.EntityEmployment;
import org.kuali.kfs.kim.impl.identity.entity.Entity;
import org.kuali.kfs.kim.impl.identity.principal.Principal;
import org.kuali.kfs.kns.rule.event.KualiAddLineEvent;
import org.kuali.kfs.kns.util.KNSGlobalVariables;
import org.kuali.kfs.kns.web.struts.form.KualiDocumentFormBase;
import org.kuali.kfs.krad.UserSessionUtils;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.rules.rule.event.RouteDocumentEvent;
import org.kuali.kfs.krad.service.KualiRuleService;
import org.kuali.kfs.krad.service.NoteService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.web.struts.FinancialSystemTransactionalDocumentActionBase;
import org.kuali.kfs.sys.service.FinancialSystemWorkflowHelperService;
import org.kuali.kfs.vnd.businessobject.VendorPhoneNumber;

import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.module.purap.CUPurapConstants;
import edu.cornell.kfs.module.purap.CUPurapKeyConstants;
import edu.cornell.kfs.module.purap.businessobject.IWantAccount;
import edu.cornell.kfs.module.purap.businessobject.IWantDocUserOptions;
import edu.cornell.kfs.module.purap.businessobject.IWantItem;
import edu.cornell.kfs.module.purap.businessobject.LevelOrganization;
import edu.cornell.kfs.module.purap.document.IWantDocument;
import edu.cornell.kfs.module.purap.document.service.IWantDocumentService;
import edu.cornell.kfs.module.purap.document.validation.event.AddIWantItemEvent;
import edu.cornell.kfs.module.purap.util.PurApFavoriteAccountLineBuilderForIWantDocument;
import edu.cornell.kfs.sys.util.ConfidentialAttachmentUtil;

@SuppressWarnings("deprecation")
public class IWantDocumentAction extends FinancialSystemTransactionalDocumentActionBase {
    private static final Logger LOG = LogManager.getLogger();
    private static final String IWANT_DEPT_ORGS_TO_EXCLUDE_PARM = "IWANT_DEPT_ORGS_TO_EXCLUDE";

    /**
     * @see org.kuali.kfs.kns.web.struts.action.KualiDocumentActionBase#loadDocument(org.kuali.kfs.kns.web.struts.form.KualiDocumentFormBase)
     */
    @Override
    protected void loadDocument(KualiDocumentFormBase kualiDocumentFormBase) {

        super.loadDocument(kualiDocumentFormBase);
        IWantDocumentForm iWantForm = (IWantDocumentForm) kualiDocumentFormBase;
        IWantDocument iWantDocument = iWantForm.getIWantDocument();

        if (StringUtils.equalsIgnoreCase(iWantForm.getPresentationMode(),
                CUPurapConstants.IWantPresentationModes.MULTIPLE_PAGE_MODE)) {
            
            if (iWantDocument.getDocumentHeader().getWorkflowDocument().isSaved()) {

                iWantForm.setStep(CUPurapConstants.IWantDocumentSteps.CUSTOMER_DATA_STEP);
                iWantDocument.setStep(CUPurapConstants.IWantDocumentSteps.CUSTOMER_DATA_STEP);

                if (StringUtils.isNotBlank(iWantDocument.getCurrentRouteToNetId())) {
                    iWantForm.getNewAdHocRoutePerson().setId(iWantDocument.getCurrentRouteToNetId());
                }

            } else if (!iWantDocument.getDocumentHeader().getWorkflowDocument().isInitiated()) {
                iWantForm.setStep(CUPurapConstants.IWantDocumentSteps.REGULAR);
                iWantDocument.setStep(CUPurapConstants.IWantDocumentSteps.REGULAR);
            }

        } else if (StringUtils.equalsIgnoreCase(iWantForm.getPresentationMode(),
                CUPurapConstants.IWantPresentationModes.FULL_PAGE_MODE)) {
            
            iWantForm.setStep(CUPurapConstants.IWantDocumentSteps.REGULAR);
            iWantDocument.setStep(CUPurapConstants.IWantDocumentSteps.REGULAR);
            
            if (iWantDocument.getDocumentHeader().getWorkflowDocument().isSaved()) {
                if (StringUtils.isNotBlank(iWantDocument.getCurrentRouteToNetId())) {
                    iWantForm.getNewAdHocRoutePerson().setId(iWantDocument.getCurrentRouteToNetId());
                }
            }
        }
    }

    /**
     * 
     * @see org.kuali.kfs.kns.web.struts.action.KualiDocumentActionBase#docHandler(org.apache.struts.action.ActionMapping,
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
        String step = request.getParameter(CUPurapConstants.IWNT_STEP_PARAMETER);
        IWantDocumentService iWantDocumentService = SpringContext.getBean(IWantDocumentService.class);

        if (step != null) {
            iWantForm.setStep(step);
        }

        if (iWantDocument != null) {
            if (iWantDocument.getDocumentHeader().getWorkflowDocument().isSaved()) {
                step = CUPurapConstants.IWantDocumentSteps.CUSTOMER_DATA_STEP;
            }

            iWantDocument.setStep(step);

            if (KewApiConstants.INITIATE_COMMAND.equalsIgnoreCase(command)) {
                iWantForm.setDocument(iWantDocument);

                if (iWantDocument != null) {

                    String principalId = iWantDocument.getDocumentHeader().getWorkflowDocument().getInitiatorPrincipalId();
                    Principal initiator = KimApiServiceLocator.getIdentityService().getPrincipal(principalId);
                    String initiatorPrincipalID = initiator.getPrincipalId();
                    String initiatorNetID = initiator.getPrincipalName();

                    iWantDocument.setInitiatorNetID(initiatorNetID);

                    Person currentUser = GlobalVariables.getUserSession().getPerson();
                    String initiatorName = currentUser.getNameUnmasked();
                    String initiatorPhoneNumber = currentUser.getPhoneNumberUnmasked();
                    String initiatorEmailAddress = currentUser.getEmailAddressUnmasked();

                    String address = iWantDocumentService.getPersonCampusAddress(initiatorNetID);

                    iWantDocument.setInitiatorName(initiatorName);
                    iWantDocument.setInitiatorPhoneNumber(initiatorPhoneNumber);
                    iWantDocument.setInitiatorEmailAddress(initiatorEmailAddress);
                    iWantDocument.setInitiatorAddress(address);

                    // check default user options
                    Map<String, String> primaryKeysCollegeOption = new HashMap<String, String>();
                    primaryKeysCollegeOption.put(CUPurapConstants.USER_OPTIONS_PRINCIPAL_ID, initiatorPrincipalID);
                    primaryKeysCollegeOption.put(CUPurapConstants.USER_OPTIONS_OPTION_ID, CUPurapConstants.USER_OPTIONS_DEFAULT_COLLEGE);
                    IWantDocUserOptions userOptionsCollege = (IWantDocUserOptions) getBusinessObjectService()
                            .findByPrimaryKey(IWantDocUserOptions.class, primaryKeysCollegeOption);

                    Map<String, String> primaryKeysDepartmentOption = new HashMap<String, String>();
                    primaryKeysDepartmentOption.put(CUPurapConstants.USER_OPTIONS_PRINCIPAL_ID, initiatorPrincipalID);
                    primaryKeysDepartmentOption.put(CUPurapConstants.USER_OPTIONS_OPTION_ID, CUPurapConstants.USER_OPTIONS_DEFAULT_DEPARTMENT);
                    IWantDocUserOptions userOptionsDepartment = (IWantDocUserOptions) getBusinessObjectService()
                            .findByPrimaryKey(IWantDocUserOptions.class, primaryKeysDepartmentOption);
                    
                    //check default deliver to address info

                    Map<String, String> primaryKeysdeliverToNetIDOption = new HashMap<String, String>();
                    primaryKeysdeliverToNetIDOption.put(CUPurapConstants.USER_OPTIONS_PRINCIPAL_ID, initiatorPrincipalID);
                    primaryKeysdeliverToNetIDOption.put(CUPurapConstants.USER_OPTIONS_OPTION_ID, CUPurapConstants.USER_OPTIONS_DEFAULT_DELIVER_TO_NET_ID);
                    IWantDocUserOptions userOptionsDeliverToNetID = (IWantDocUserOptions) getBusinessObjectService()
                            .findByPrimaryKey(IWantDocUserOptions.class, primaryKeysdeliverToNetIDOption);

                    Map<String, String> primaryKeysDeliverToNameOption = new HashMap<String, String>();
                    primaryKeysDeliverToNameOption.put(CUPurapConstants.USER_OPTIONS_PRINCIPAL_ID, initiatorPrincipalID);
                    primaryKeysDeliverToNameOption.put(CUPurapConstants.USER_OPTIONS_OPTION_ID, CUPurapConstants.USER_OPTIONS_DEFAULT_DELIVER_TO_NAME);
                    IWantDocUserOptions userOptionsDeliverToName = (IWantDocUserOptions) getBusinessObjectService()
                            .findByPrimaryKey(IWantDocUserOptions.class, primaryKeysDeliverToNameOption);
                    
                    Map<String, String> primaryKeysDeliverToEmailOption = new HashMap<String, String>();
                    primaryKeysDeliverToEmailOption.put(CUPurapConstants.USER_OPTIONS_PRINCIPAL_ID, initiatorPrincipalID);
                    primaryKeysDeliverToEmailOption.put(CUPurapConstants.USER_OPTIONS_OPTION_ID,
                            CUPurapConstants.USER_OPTIONS_DEFAULT_DELIVER_TO_EMAIL_ADDRESS);
                    IWantDocUserOptions userOptionsDeliverToEmail = (IWantDocUserOptions) getBusinessObjectService()
                            .findByPrimaryKey(IWantDocUserOptions.class, primaryKeysDeliverToEmailOption);
                    
                    Map<String, String> primaryKeysDeliverToPhnNbrOption = new HashMap<String, String>();
                    primaryKeysDeliverToPhnNbrOption.put(CUPurapConstants.USER_OPTIONS_PRINCIPAL_ID, initiatorPrincipalID);
                    primaryKeysDeliverToPhnNbrOption.put(CUPurapConstants.USER_OPTIONS_OPTION_ID,
                            CUPurapConstants.USER_OPTIONS_DEFAULT_DELIVER_TO_PHONE_NUMBER);
                    IWantDocUserOptions userOptionsDeliverToPhnNbr = (IWantDocUserOptions) getBusinessObjectService()
                            .findByPrimaryKey(IWantDocUserOptions.class, primaryKeysDeliverToPhnNbrOption);
                    
                    Map<String, String> primaryKeysDeliverToAddressOption = new HashMap<String, String>();
                    primaryKeysDeliverToAddressOption.put(CUPurapConstants.USER_OPTIONS_PRINCIPAL_ID, initiatorPrincipalID);
                    primaryKeysDeliverToAddressOption.put(CUPurapConstants.USER_OPTIONS_OPTION_ID, CUPurapConstants.USER_OPTIONS_DEFAULT_DELIVER_TO_ADDRESS);
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

                iWantDocumentService.setIWantDocumentDescription(iWantDocument);
            }
        }

        return actionForward;
    }

    private boolean determineDocStatusForCollegeDepartmentButtons(IWantDocument iWantDocument) {
        if (iWantDocument.getDocumentHeader().getWorkflowDocument().isInitiated()
                | iWantDocument.getDocumentHeader().getWorkflowDocument().isSaved()) {
            return true;
        }
        return false;
    }

    private boolean userIsInitiator(IWantDocument iWantDocument) {
        String documentInitiatorNetid = iWantDocument.getInitiatorNetID();

        if (StringUtils.isBlank(documentInitiatorNetid)) {
            return true;
        }

        Person currentUser = GlobalVariables.getUserSession().getPerson();
        String currentUserNetId = currentUser.getPrincipalName();

        if (StringUtils.isNotBlank(currentUserNetId)
                && StringUtils.equalsIgnoreCase(documentInitiatorNetid, currentUserNetId)) {
            return true;
        }
        return false;
    }

    /**
     *
     * @see org.kuali.kfs.kns.web.struts.action.KualiAction#execute(org.apache.struts.action.ActionMapping,
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

            // populate department drop down
            if (!documentForm.getPreviousSelectedOrg().equalsIgnoreCase(((IWantDocument) documentForm.getDocument()).getCollegeLevelOrganization())) {
                String cLevelOrg = ((IWantDocument) documentForm.getDocument()).getCollegeLevelOrganization();

                documentForm.getDeptOrgKeyLabels().clear();
                documentForm.getDeptOrgKeyLabels().add(new ConcreteKeyValue("", "Please Select"));

                if (StringUtils.isNotEmpty(cLevelOrg)) {

                    IWantDocumentService iWantDocumentService = SpringContext.getBean(IWantDocumentService.class);
                    List<LevelOrganization> dLevelOrgs = iWantDocumentService.getDLevelOrganizations(cLevelOrg);

                    // Get the list of chart+org combos to forcibly exclude from the drop-down, if any.
                    String routingChart = ((IWantDocument) documentForm.getDocument()).getRoutingChart();
                    Collection<String> dLevelExcludesList = getParameterService().getParameterValuesAsString(PurapConstants.PURAP_NAMESPACE,
                            KRADConstants.DetailTypes.DOCUMENT_DETAIL_TYPE, IWANT_DEPT_ORGS_TO_EXCLUDE_PARM);
                    Set<String> dLevelExcludes =
                            new HashSet<String>((dLevelExcludesList != null) ? dLevelExcludesList : Collections.<String>emptyList());

                    for (LevelOrganization levelOrganization : dLevelOrgs) {

                        // Add each department-level org to the drop-down as long as it is not marked for exclusion.
                        if (!dLevelExcludes.contains(routingChart + "=" + levelOrganization.getCode())) {
                            documentForm.getDeptOrgKeyLabels().add(
                                    new ConcreteKeyValue(levelOrganization.getCode(), levelOrganization.getCodeAndDescription()));
                        }
                    }
                }
            }
            setupDocumentMessages(documentForm.getStep());

            //Set flags used to determine "Set as Default" and "Reset Initiator Defaults" usability on the document.
            documentForm.setDocIsInitiatedOrSaved(determineDocStatusForCollegeDepartmentButtons(iWantDoc));
            documentForm.setUserMatchesInitiator(userIsInitiator(iWantDoc));
        }

        return actionForward;
    }

    private void setupDocumentMessages(String step) {
        if (CUPurapConstants.IWantDocumentSteps.CUSTOMER_DATA_STEP.equals(step)) {
            KNSGlobalVariables.getMessageList().add(KFSKeyConstants.ERROR_CUSTOM, "Welcome to the I Want Doc! Submit your order request in just 4 easy steps.");
            KNSGlobalVariables.getMessageList().add(KFSKeyConstants.ERROR_CUSTOM, "I Want Document Step #1");
        } else if (CUPurapConstants.IWantDocumentSteps.ITEMS_AND_ACCT_DATA_STEP.equals(step)) {
            KNSGlobalVariables.getMessageList().add(KFSKeyConstants.ERROR_CUSTOM, "I Want Document Step #2");
        } else if (CUPurapConstants.IWantDocumentSteps.VENDOR_STEP.equals(step)) {
            KNSGlobalVariables.getMessageList().add(KFSKeyConstants.ERROR_CUSTOM, "I Want Document Step #3");
        } else if (CUPurapConstants.IWantDocumentSteps.ROUTING_STEP.equals(step)) {
            KNSGlobalVariables.getMessageList().add(KFSKeyConstants.ERROR_CUSTOM, "I Want Document Step #4");
        }
    }


    /**
     * Sets the College and Department based on the initiator primary department.
     * 
     * @param documentForm
     */
    private void setCollegeAndDepartmentBasedOnPrimaryDepartment(IWantDocumentForm documentForm) {
        IWantDocumentService iWantDocumentService = SpringContext.getBean(IWantDocumentService.class);
        String primaryDeptOrg = null;

        IWantDocument iWantDocument = null;
        if (documentForm != null && documentForm.getDocument() != null) {
            iWantDocument = (IWantDocument) documentForm.getDocument();
        }
        
        if (iWantDocument != null && StringUtils.isEmpty(iWantDocument.getCollegeLevelOrganization())) {

            String principalIdToUseForCollegeDeptLookup = null;
            if (StringUtils.isNotBlank(iWantDocument.getDocumentHeader().getWorkflowDocument().getInitiatorPrincipalId())) {
                principalIdToUseForCollegeDeptLookup = iWantDocument.getDocumentHeader().getWorkflowDocument().getInitiatorPrincipalId();
            } else {
                Person currentUser = GlobalVariables.getUserSession().getPerson();
                principalIdToUseForCollegeDeptLookup = currentUser.getPrincipalId();
            }

            Entity entityInfo = KimApiServiceLocator.getIdentityService().getEntityByPrincipalId(principalIdToUseForCollegeDeptLookup);

            if (ObjectUtils.isNotNull(entityInfo)) {
                if (ObjectUtils.isNotNull(entityInfo.getEmploymentInformation()) && entityInfo.getEmploymentInformation().size() > 0) {
                    EntityEmployment employmentInformation = entityInfo.getEmploymentInformation().get(0);
                    String primaryDepartment = employmentInformation.getPrimaryDepartmentCode();
                    primaryDeptOrg = primaryDepartment.substring(primaryDepartment.lastIndexOf('-') + 1,
                            primaryDepartment.length());

                    String cLevelOrg = iWantDocumentService.getCLevelOrganizationForDLevelOrg(primaryDepartment);
                    ((IWantDocument) documentForm.getDocument()).setCollegeLevelOrganization(cLevelOrg);
                }
            }
        }

        if (iWantDocument != null && StringUtils.isNotEmpty(iWantDocument.getCollegeLevelOrganization())) {
            String cLevelOrg = iWantDocument.getCollegeLevelOrganization();
            documentForm.getDeptOrgKeyLabels().clear();
            documentForm.getDeptOrgKeyLabels().add(new ConcreteKeyValue("", "Please Select"));

            List<LevelOrganization> dLevelOrgs = iWantDocumentService.getDLevelOrganizations(cLevelOrg);

            for (LevelOrganization levelOrganization : dLevelOrgs) {
                documentForm.getDeptOrgKeyLabels().add(
                        new ConcreteKeyValue(levelOrganization.getCode(), levelOrganization.getCodeAndDescription()));

            }

            if (primaryDeptOrg != null) {
                iWantDocument.setDepartmentLevelOrganization(primaryDeptOrg);
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
    
    public ActionForward resetInitiatorDefaultCollegeAndDepartment(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        IWantDocumentForm iWantForm = (IWantDocumentForm) form;
        IWantDocument iWantDocument = null;

        if (iWantForm != null && iWantForm.getDocument() != null) {

            iWantDocument = (IWantDocument) iWantForm.getDocument();

            iWantDocument.setCollegeLevelOrganization(KFSConstants.EMPTY_STRING);
            iWantDocument.setDepartmentLevelOrganization(KFSConstants.EMPTY_STRING);

            setCollegeAndDepartmentBasedOnPrimaryDepartment(iWantForm);
            iWantDocument.setUseCollegeAndDepartmentAsDefault(true);
        }

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    public ActionForward switchToFullPagePresentation(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        IWantDocumentForm iWantForm = (IWantDocumentForm) form;
        IWantDocument iWantDocument = iWantForm.getIWantDocument();
        
        iWantForm.setStep(CUPurapConstants.IWantDocumentSteps.REGULAR);
        iWantDocument.setStep(CUPurapConstants.IWantDocumentSteps.REGULAR);
        
        iWantForm.setPresentationMode(CUPurapConstants.IWantPresentationModes.FULL_PAGE_MODE);

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    public ActionForward switchToMultipleStepsPresentation(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        IWantDocumentForm iWantForm = (IWantDocumentForm) form;
        IWantDocument iWantDocument = iWantForm.getIWantDocument();

        if (iWantDocument.getDocumentHeader().getWorkflowDocument().isSaved() ||
                iWantDocument.getDocumentHeader().getWorkflowDocument().isInitiated()) {
        
            iWantForm.setStep(CUPurapConstants.IWantDocumentSteps.CUSTOMER_DATA_STEP);
            iWantDocument.setStep(CUPurapConstants.IWantDocumentSteps.CUSTOMER_DATA_STEP);
        
            iWantForm.setPresentationMode(CUPurapConstants.IWantPresentationModes.MULTIPLE_PAGE_MODE);
        }

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
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
        boolean added = true;
        
        //add new item and new accounting line if not empty
        IWantItem item = iWantForm.getNewIWantItemLine();
        if (StringUtils.isNotBlank(item.getItemDescription()) || item.getItemUnitPrice() != null || item.getItemQuantity() != null) {
            added &= addNewItem(iWantForm, iWantDocument, item);
        }
        
        added &= addNewFavoriteAccountIfNecessary(added, iWantDocument);
        
        if (added) {
            IWantAccount account = iWantForm.getNewSourceLine();
            if (StringUtils.isNotBlank(account.getAccountNumber()) || StringUtils.isNotBlank(account.getSubAccountNumber())
                    || StringUtils.isNotBlank(account.getFinancialObjectCode()) || StringUtils.isNotBlank(account.getFinancialSubObjectCode())
                    || StringUtils.isNotBlank(account.getProjectCode()) || StringUtils.isNotBlank(account.getOrganizationReferenceId())) {
                added &= addNewAccount(iWantForm, iWantDocument, account);
            }
        }

        // If addition of IWNT item or account failed, then skip the rest of the validation.
        if (!added) {
            return mapping.findForward(KFSConstants.MAPPING_BASIC);
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
     * @see org.kuali.kfs.kns.web.struts.action.KualiDocumentActionBase#close(org.apache.struts.action.ActionMapping,
     * org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    public ActionForward close(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
    	// KFSPTS-3606 : Always redirect to Action List on close.
    	((IWantDocumentForm) form).setReturnToActionList(true);
    	return super.close(mapping, form, request, response);
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
     * @see org.kuali.kfs.kns.web.struts.action.KualiAction#getSelectedLine(javax.servlet.http.HttpServletRequest)
     */
    protected int getSelectedLine(HttpServletRequest request) {
        int selectedLine = -1;
        String parameterName = (String) request.getAttribute(KRADConstants.METHOD_TO_CALL_ATTRIBUTE);
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
    
    private boolean initiatorEnteredOwnNetidAsApprover(ActionForm form) {
        IWantDocumentForm iWantDocForm = (IWantDocumentForm) form;
        String enteredApproverNetID = iWantDocForm.getNewAdHocRoutePerson().getId();
        
        IWantDocument iWantDocument = iWantDocForm.getIWantDocument();
        String initiatorNetID = iWantDocument.getInitiatorNetID();

        if (StringUtils.equalsIgnoreCase(initiatorNetID, enteredApproverNetID)) {
            GlobalVariables.getMessageMap().putError(KRADConstants.GLOBAL_ERRORS, CUPurapKeyConstants.ERROR_IWNT_CREATOR_CANNOT_ROUTE_TO_SELF);
            return true;
        }
        return false;
    }
    
    private boolean approverEnteredOwnNetidAsApprover(ActionForm form) {
        IWantDocumentForm iWantDocForm = (IWantDocumentForm) form;
        String enteredApproverNetID = iWantDocForm.getNewAdHocRoutePersonNetId();
        
        Person currentUser = GlobalVariables.getUserSession().getPerson();
        String currentUserNetID = currentUser.getPrincipalName();

        if (StringUtils.equalsIgnoreCase(currentUserNetID, enteredApproverNetID)) {
            GlobalVariables.getMessageMap().putError(KRADConstants.GLOBAL_ERRORS, CUPurapKeyConstants.ERROR_IWNT_APPROVER_CANNOT_ROUTE_TO_SELF);
            return true;
        }
        return false;
    }
    
    /**
     * 
     * @see org.kuali.kfs.kns.web.struts.action.KualiDocumentActionBase#route(org.apache.struts.action.ActionMapping,
     * org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    @Override
    public ActionForward route(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        IWantDocumentForm iWantDocForm = (IWantDocumentForm) form;
        IWantDocument iWantDocument = iWantDocForm.getIWantDocument();
        IWantDocumentService iWantDocumentService = SpringContext.getBean(IWantDocumentService.class);
        boolean added = true;
        
        if (initiatorEnteredOwnNetidAsApprover(form)) {
            return mapping.findForward(KFSConstants.MAPPING_BASIC);
        }
        
        //add new item and new accounting line if not empty
        IWantItem item = iWantDocForm.getNewIWantItemLine();
        if (StringUtils.isNotBlank(item.getItemDescription()) || item.getItemUnitPrice() != null || item.getItemQuantity() != null) {
            added &= addNewItem(iWantDocForm, iWantDocument, item);
        }
        
        added &= addNewFavoriteAccountIfNecessary(added, iWantDocument);
        
        if (added) {
            IWantAccount account = iWantDocForm.getNewSourceLine();
            if (StringUtils.isNotBlank(account.getAccountNumber()) || StringUtils.isNotBlank(account.getSubAccountNumber())
                    || StringUtils.isNotBlank(account.getFinancialObjectCode()) || StringUtils.isNotBlank(account.getFinancialSubObjectCode())
                    || StringUtils.isNotBlank(account.getProjectCode()) || StringUtils.isNotBlank(account.getOrganizationReferenceId())) {
                added &= addNewAccount(iWantDocForm, iWantDocument, account);
            }
        }

        // Do not route if there were failures adding new items or accounts.
        if (!added) {
            return mapping.findForward(KFSConstants.MAPPING_BASIC);
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
        
        if (iWantDocument.isSetDeliverToInfoAsDefault()) {
            
            if (StringUtils.isNotBlank(iWantDocument.getDeliverToNetID())) {
                saveUserOption(principalId, CUPurapConstants.USER_OPTIONS_DEFAULT_DELIVER_TO_NET_ID, iWantDocument.getDeliverToNetID());
            }
            
            if (StringUtils.isNotBlank(iWantDocument.getDeliverToName())) {
                saveUserOption(principalId, CUPurapConstants.USER_OPTIONS_DEFAULT_DELIVER_TO_NAME, iWantDocument.getDeliverToName());
            }
            
            if (StringUtils.isNotBlank(iWantDocument.getDeliverToEmailAddress())) {
                saveUserOption(principalId, CUPurapConstants.USER_OPTIONS_DEFAULT_DELIVER_TO_EMAIL_ADDRESS, iWantDocument.getDeliverToEmailAddress());
            }
            
            if (StringUtils.isNotBlank(iWantDocument.getDeliverToPhoneNumber())) {
                saveUserOption(principalId, CUPurapConstants.USER_OPTIONS_DEFAULT_DELIVER_TO_PHONE_NUMBER, iWantDocument.getDeliverToPhoneNumber());
            }
            
            if (StringUtils.isNotBlank(iWantDocument.getDeliverToAddress())) {
                saveUserOption(principalId, CUPurapConstants.USER_OPTIONS_DEFAULT_DELIVER_TO_ADDRESS, iWantDocument.getDeliverToAddress());
            }
            
        }

        iWantDocumentService.setIWantDocumentDescription(iWantDocument);

        //insert adhoc route person first and the route
        if (StringUtils.isNotBlank(iWantDocForm.getNewAdHocRoutePerson().getId())) {
            iWantDocument.setCurrentRouteToNetId(iWantDocForm.getNewAdHocRoutePerson().getId());
            insertAdHocRoutePerson(mapping, iWantDocForm, request, response);

        }
        ActionForward actionForward = super.route(mapping, form, request, response);

        if (CUPurapConstants.IWantDocumentSteps.ROUTING_STEP.equalsIgnoreCase(step)) {
            iWantDocForm.setStep(CUPurapConstants.IWantDocumentSteps.REGULAR);
            iWantDocument.setStep(CUPurapConstants.IWantDocumentSteps.REGULAR);

            return mapping.findForward("finish");
        }

        return actionForward;
    }
    
    private void saveUserOption(String principalId, String userOptionName, String userOptionValue) {
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

        //insert adhoc route person first and the route
        if (StringUtils.isNotBlank(iWantDocForm.getNewAdHocRoutePerson().getId())) {
            insertAdHocRoutePerson(mapping, iWantDocForm, request, response);

        }
        return super.sendAdHocRequests(mapping, form, request, response);
    }

    @Override
    public ActionForward approve(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        
        if (approverEnteredOwnNetidAsApprover(form)) {
            return mapping.findForward(KFSConstants.MAPPING_BASIC);
        }

        IWantDocumentForm iWantDocForm = (IWantDocumentForm) form;
        //insert adhoc route person first and then approve
        if (StringUtils.isNotBlank(iWantDocForm.getNewAdHocRoutePerson().getId())) {
            insertAdHocRoutePerson(mapping, iWantDocForm, request, response);
        }
        
        IWantDocument iwantDoc = iWantDocForm.getIWantDocument();
        boolean shouldAddNote = getFinancialSystemWorkflowHelperService().isAdhocApprovalRequestedForPrincipal(
                iwantDoc.getDocumentHeader().getWorkflowDocument(), GlobalVariables.getUserSession().getPrincipalId()) && 
                StringUtils.isNotBlank(iwantDoc.getCompleteOption());
        LOG.debug("approve, shouldAddNote: {}", shouldAddNote);

        ActionForward forward = super.approve(mapping, form, request, response);
        
        if (shouldAddNote) {
            addCompleteOptionNote(iwantDoc);
        }
        
        return forward;
    }

    private void addCompleteOptionNote(IWantDocument iwantDoc) {
        Person loggedInUser = GlobalVariables.getUserSession().getPerson();
        if (StringUtils.equalsIgnoreCase(iwantDoc.getCompleteOption(), CuFPConstants.YES)) {
            String noteText = getConfigurationService().getPropertyValueAsString(CUPurapKeyConstants.MESSAGE_IWANT_DOCUMENT_APPROVE_FINALIZED);
            Note note = getDocumentService().createNoteFromDocument(iwantDoc, noteText);
            note.setAuthorUniversalIdentifier(loggedInUser.getPrincipalId());
            Note savedNote = getNoteService().save(note);
            iwantDoc.addNote(savedNote);
            LOG.debug("addCompleteOptionNote, adding note to I want document {} with a text of {}", iwantDoc.getDocumentNumber(), noteText);
        } else {
            LOG.debug("addCompleteOptionNote. submit for futher workflow was selected, no note need be added");
        }
    }
    
    protected FinancialSystemWorkflowHelperService getFinancialSystemWorkflowHelperService() {
        return SpringContext.getBean(FinancialSystemWorkflowHelperService.class);
    }
    
    protected ConfigurationService getConfigurationService() {
        return SpringContext.getBean(ConfigurationService.class);
    }
    
    protected NoteService getNoteService() {
        return SpringContext.getBean(NoteService.class);
    }
    
    /**
     * Use the new attachment description field to set the note text.
     * 
     * @see org.kuali.kfs.kns.web.struts.action.KualiDocumentActionBase#insertBONote(org.apache.struts.action.ActionMapping,
     * org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    @Override
    public ActionForward insertBONote(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        IWantDocumentForm iWantDocumentForm = (IWantDocumentForm) form;
        Note note = iWantDocumentForm.getNewNote();
        
        // If trying to add a conf attachment without authorization or not properly flagging a potentially-conf attachment, then treat as a validation failure.
        if (!ConfidentialAttachmentUtil.attachmentIsNonConfidentialOrCanAddConfAttachment(note, iWantDocumentForm.getDocument(),
                iWantDocumentForm.getAttachmentFile(), getDocumentHelperService().getDocumentAuthorizer(iWantDocumentForm.getDocument()))) {
            // Just return without adding the note/attachment. The ConfidentialAttachmentUtil method will handle updating the message map accordingly.
            return mapping.findForward(KFSConstants.MAPPING_BASIC);
        }
        
        // If the note text is blank, set the attachment description as the text. Otherwise, concatenate both to form the text.
        if (StringUtils.isBlank(iWantDocumentForm.getIWantDocument().getNoteTextOption())) {
            if (StringUtils.isBlank(note.getNoteText())) {
                note.setNoteText(iWantDocumentForm.getIWantDocument().getAttachmentDescription());
            } else {
                note.setNoteText(iWantDocumentForm.getIWantDocument().getAttachmentDescription() + ": " + note.getNoteText());
            }
        } else {
            note.setNoteText(iWantDocumentForm.getIWantDocument().getNoteTextOption());
        }
        
        ActionForward actionForward = super.insertBONote(mapping, form, request, response);
        
        iWantDocumentForm.getIWantDocument().setAttachmentDescription(StringUtils.EMPTY);
        iWantDocumentForm.getIWantDocument().setNoteTextOption(StringUtils.EMPTY);

        return actionForward;
    }

    /**
     * Overridden to guarantee that form of copied document is set to whatever the entry
     * mode of the document is
     * @see org.kuali.kfs.kns.web.struts.action.KualiTransactionalDocumentActionBase#copy(org.apache.struts.action.ActionMapping,
     * org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    @Override
    public ActionForward copy(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        ActionForward forward = null;

        if (request.getParameter(KRADConstants.PARAMETER_DOC_ID) == null) {
            IWantDocumentForm iWantForm = (IWantDocumentForm) form;
            iWantForm.setPresentationMode(CUPurapConstants.IWantPresentationModes.FULL_PAGE_MODE);
            
            forward = super.copy(mapping, form, request, response);
        } else {
            // this is copy document from Procurement Gateway:
            // use this link to call: http://localhost:8080/kfs-dev/purapIWant.do?methodToCall=copy&docId=xxxx
            String docId = request.getParameter(KRADConstants.PARAMETER_DOC_ID);
            KualiDocumentFormBase kualiDocumentFormBase = (KualiDocumentFormBase) form;

            IWantDocument document = null;
            document = (IWantDocument) getDocumentService().getByDocumentHeaderId(docId);
            document.toCopyFromGateway();

            IWantDocumentForm iWantForm = (IWantDocumentForm) form;
            iWantForm.setPresentationMode(CUPurapConstants.IWantPresentationModes.FULL_PAGE_MODE);
            
            kualiDocumentFormBase.setDocument(document);
            WorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
            kualiDocumentFormBase.setDocTypeName(workflowDocument.getDocumentTypeName());
            UserSessionUtils.addWorkflowDocument(GlobalVariables.getUserSession(), workflowDocument);

            forward = mapping.findForward(KFSConstants.MAPPING_BASIC);
        }
        return forward;
    }

    /**
     * 
     * @see org.kuali.kfs.kns.web.struts.action.KualiDocumentActionBase#refresh(org.apache.struts.action.ActionMapping,
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

            iWantDocument.setDeliverToAddress(getPersonCampusAddressForRefresh(iWantDocument.getDeliverToNetID()));
            iWantDocument.setInitiatorAddress(getPersonCampusAddressForRefresh(iWantDocument.getInitiatorNetID()));

            if ("iWantDocVendorLookupable".equalsIgnoreCase(refreshCaller)) {
                Integer vendorHeaderId = iWantDocument.getVendorHeaderGeneratedIdentifier();
                Integer vendorId = iWantDocument.getVendorDetailAssignedIdentifier();
                String phoneNumber = "Phone: ";

                Map<String,Object> fieldValues = new HashMap<String,Object>();
                fieldValues.put("vendorHeaderGeneratedIdentifier", vendorHeaderId);
                fieldValues.put("vendorDetailAssignedIdentifier", vendorId);
                fieldValues.put("vendorPhoneTypeCode", "PH");
                Collection<VendorPhoneNumber> vendorPhoneNumbers = getBusinessObjectService().findMatching(VendorPhoneNumber.class,
                        fieldValues);
                if (ObjectUtils.isNotNull(vendorPhoneNumbers) && vendorPhoneNumbers.size() > 0) {
                    VendorPhoneNumber retrievedVendorPhoneNumber = vendorPhoneNumbers.toArray(new VendorPhoneNumber[1])[0];
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

                String url = "URL: "
                        + (iWantDocument.getVendorWebURL() != null ? iWantDocument.getVendorWebURL()
                                : StringUtils.EMPTY);

                String vendorInfo = new StringBuilder(100).append(addressLine1).append('\n').append(
                            addressLine2).append('\n').append(
                            cityName).append(", ").append(postalCode).append(", ").append(stateCode).append(", ").append(countryCode).append('\n').append(
                            faxNumber).append('\n').append(
                            phoneNumber).append(" \n").append(
                            url).toString();

                iWantDocument.setVendorDescription(vendorInfo);
            }

        }
        return actionForward;
    }

    private String getPersonCampusAddressForRefresh(String netId) {
        String address = KFSConstants.EMPTY_STRING;
        if (StringUtils.isNotEmpty(netId)) {
            IWantDocumentService iWantDocumentService = SpringContext.getBean(IWantDocumentService.class);
            address = iWantDocumentService.getPersonCampusAddress(netId);
        }
        return address;
    }

    @Override
    public ActionForward save(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
    	IWantDocumentService iWantDocumentService = SpringContext.getBean(IWantDocumentService.class);

        // Only recreate document description if in INITIATED or SAVED status.
        WorkflowDocument workflowDocument = ((KualiDocumentFormBase) form).getDocument().getDocumentHeader().getWorkflowDocument();
        if (workflowDocument.isInitiated() || workflowDocument.isSaved()) {
            iWantDocumentService.setIWantDocumentDescription((IWantDocument) ((KualiDocumentFormBase) form).getDocument());
        }

        ActionForward actionForward = super.save(mapping, form, request, response);
        IWantDocumentForm iWantDocForm = (IWantDocumentForm) form;
        IWantDocument iWantDocument = iWantDocForm.getIWantDocument();
        boolean added = true;

        //add new item and new accounting line if not empty
        IWantItem item = iWantDocForm.getNewIWantItemLine();
        if (StringUtils.isNotBlank(item.getItemDescription()) || item.getItemUnitPrice() != null || item.getItemQuantity() != null) {
            added &= addNewItem(iWantDocForm, iWantDocument, item);
        }
        
        added &= addNewFavoriteAccountIfNecessary(added, iWantDocument);
        
        if (added) {
            IWantAccount account = iWantDocForm.getNewSourceLine();
            if (StringUtils.isNotBlank(account.getAccountNumber()) || StringUtils.isNotBlank(account.getSubAccountNumber())
                    || StringUtils.isNotBlank(account.getFinancialObjectCode()) || StringUtils.isNotBlank(account.getFinancialSubObjectCode())
                    || StringUtils.isNotBlank(account.getProjectCode()) || StringUtils.isNotBlank(account.getOrganizationReferenceId())) {
                added &= addNewAccount(iWantDocForm, iWantDocument, account);
            }
        }

        // Do not save if item or account additions failed.
        if (!added) {
            return mapping.findForward(KFSConstants.MAPPING_BASIC);
        }

        iWantDocument.setExplanation(iWantDocument.getDocumentHeader().getExplanation());

        if (StringUtils.isNotBlank(iWantDocForm.getNewAdHocRoutePerson().getId())) {
            iWantDocument.setCurrentRouteToNetId(iWantDocForm.getNewAdHocRoutePerson().getId());
        }

        return actionForward;
    }

    /**
     * Adds a new IWNT account to the document using the selected Favorite Account from the drop-down.
     * This is similar to a method on PurchasingActionBase, but has been modified for IWNT use instead.
     */
    public ActionForward addFavoriteAccount(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        IWantDocumentForm iwntForm = (IWantDocumentForm) form;
        IWantDocument document = iwntForm.getIWantDocument();
        
        if (addNewFavoriteAccount(document)) {
            document.setFavoriteAccountLineIdentifier(null);
        }
        
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * Redirects the user to a URL that creates a new REQS doc with data from the current IWantDocument.
     * However, due to configuration on the associated button from the IWantDocumentForm, client-side
     * JavaScript should handle the redirect for us and in a separate window/tab, unless the client has
     * disabled JavaScript.
     */
    public ActionForward createRequisition(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        IWantDocumentForm iWantDocForm = (IWantDocumentForm) form;
        IWantDocument iWantDocument = iWantDocForm.getIWantDocument();

        // Make sure a related requisition does not already exist before creating one.
        if (StringUtils.isNotBlank(iWantDocument.getReqsDocId())) {
            GlobalVariables.getMessageMap().putError(KRADConstants.GLOBAL_ERRORS, CUPurapKeyConstants.ERROR_IWNT_REQUISITION_EXISTS);
            return mapping.findForward(KFSConstants.MAPPING_BASIC);
        }

        String url = ConfigContext.getCurrentContextConfig().getProperty(KFSConstants.APPLICATION_URL_KEY)
                + "/purapRequisition.do?methodToCall=createReqFromIWantDoc&docId=" + iWantDocument.getDocumentNumber();

        ActionForward actionForward = new ActionForward(url, true);

        return actionForward;
    }

    private boolean addNewAccount(IWantDocumentForm iWantDocumentForm, IWantDocument iWantDoc, IWantAccount account) {

        KualiRuleService ruleService = SpringContext.getBean(KualiRuleService.class);
        boolean acctRulesPassed = true;

        acctRulesPassed &= ruleService.applyRules(new KualiAddLineEvent(iWantDoc, "accounts", account));

        if (acctRulesPassed) {
            account = iWantDocumentForm.getAndResetNewIWantAccountLine();
            iWantDoc.addAccount(account);
        }  
        
        return acctRulesPassed;
        
    }
    
    private boolean addNewItem(IWantDocumentForm iWantDocumentForm, IWantDocument iWantDoc, IWantItem item) {

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

    /**
     * Adds a new Favorite Account line to the document for a document-save/route or move-to-next-step action,
     * if the user selected a Favorite Account on the drop-down but did not click the "add" button, and if
     * any prior auto-line-addition operations for other lists have also succeeded or skipped accordingly.
     * 
     * @param added An indicator of whether any prior auto-line-addition actions succeeded or are unneeded.
     * @param iWantDocument The document to add the line to.
     * @return True if the given flag is true and the Favorite Account is either added successfully or doesn't need to be added, false otherwise.
     */
    private boolean addNewFavoriteAccountIfNecessary(boolean added, IWantDocument iWantDocument) {
        if (added && iWantDocument.getFavoriteAccountLineIdentifier() != null) {
            added &= addNewFavoriteAccount(iWantDocument);
            if (added) {
                iWantDocument.setFavoriteAccountLineIdentifier(null);
            }
        }
        return added;
    }

    /**
     * Adds a new Favorite Account line to the IWantDocument.
     * 
     * @param iWantDocument The document to add the line to.
     * @return true if the Favorite-Account-based line was added successfully, false otherwise.
     */
    private boolean addNewFavoriteAccount(IWantDocument iWantDocument) {
        int numErrors = GlobalVariables.getMessageMap().getErrorCount();
        
        new PurApFavoriteAccountLineBuilderForIWantDocument(iWantDocument).addNewFavoriteAccountLineToListIfPossible();
        
        return numErrors == GlobalVariables.getMessageMap().getErrorCount();
    }

}
