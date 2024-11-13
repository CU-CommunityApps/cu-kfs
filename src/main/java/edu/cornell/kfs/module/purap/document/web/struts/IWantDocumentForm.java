package edu.cornell.kfs.module.purap.document.web.struts;

import edu.cornell.kfs.module.purap.CUPurapConstants;
import edu.cornell.kfs.module.purap.CUPurapKeyConstants;
import edu.cornell.kfs.module.purap.businessobject.IWantAccount;
import edu.cornell.kfs.module.purap.businessobject.IWantItem;
import edu.cornell.kfs.module.purap.document.IWantDocument;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.kns.web.ui.ExtraButton;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.web.struts.FinancialSystemTransactionalDocumentFormBase;
import org.kuali.kfs.core.api.config.property.ConfigContext;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.api.util.KeyValue;
import org.kuali.kfs.kew.api.WorkflowDocument;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@SuppressWarnings("deprecation")
public class IWantDocumentForm extends FinancialSystemTransactionalDocumentFormBase {

    private static final long serialVersionUID = -82175061546434849L;
    
    protected boolean isWizard;
    protected String step;
    protected String presentationMode;
    protected String headerTitle;

    protected IWantItem newIWantItemLine;
    protected IWantAccount newSourceLine;

    protected List<KeyValue> deptOrgKeyLabels;
    protected String previousSelectedOrg = KFSConstants.EMPTY_STRING;

    protected boolean userMatchesInitiator;
    protected boolean docIsInitiatedOrSaved;

    public IWantDocumentForm() {
        super();
        setNewIWantItemLine(new IWantItem());
        newSourceLine = new IWantAccount();
        this.setDeptOrgKeyLabels(new ArrayList<KeyValue>());
        editingMode = new HashMap<Object,Object>();
        presentationMode = CUPurapConstants.IWantPresentationModes.MULTIPLE_PAGE_MODE;
        userMatchesInitiator = false;
        docIsInitiatedOrSaved = false;
    }

    @Override
    protected String getDefaultDocumentTypeName() {
        return "IWNT";
    }

    public IWantDocument getIWantDocument() {
        return (IWantDocument) getDocument();
    }

    public void setIWantDocument(IWantDocument iWantDocument) {
        setDocument(iWantDocument);
    }

    public String getLineItemImportInstructionsUrl() {
        return "";
    }

    public IWantItem getNewIWantItemLine() {
        return newIWantItemLine;
    }

    public void setNewIWantItemLine(IWantItem newIWantItemLine) {
        this.newIWantItemLine = newIWantItemLine;
    }

    /**
     * Returns the new IWant Item Line and resets it to null.
     * 
     * @return the new IWant Item Line.
     */
    public IWantItem getAndResetNewIWantItemLine() {
        IWantItem aIWantItem = getNewIWantItemLine();
        setNewIWantItemLine(new IWantItem());
        return aIWantItem;
    }

    /**
     * This method ...
     * 
     * @return
     */
    public IWantAccount getAndResetNewIWantAccountLine() {
        IWantAccount aIWantAccount = (IWantAccount) getNewSourceLine();
        newSourceLine = new IWantAccount();
        return aIWantAccount;
    }

    public List<KeyValue> getDeptOrgKeyLabels() {
        return deptOrgKeyLabels;
    }

    public void setDeptOrgKeyLabels(List<KeyValue> deptOrgKeyLabels) {
        this.deptOrgKeyLabels = deptOrgKeyLabels;
    }

    public String getPreviousSelectedOrg() {
        return previousSelectedOrg;
    }

    public void setPreviousSelectedOrg(String previousSelectedOrg) {
        this.previousSelectedOrg = previousSelectedOrg;
    }

    public IWantAccount getNewSourceLine() {
        return newSourceLine;
    }

    public void setNewSourceLine(IWantAccount newSourceLine) {
        this.newSourceLine = newSourceLine;
    }

    public boolean isIsWizard() {
        return isWizard;
    }

    public void setIsWizard(boolean isWizard) {
        this.isWizard = isWizard;
    }

    public String getStep() {
        if (CUPurapConstants.IWantDocumentSteps.CUSTOMER_DATA_STEP.equalsIgnoreCase(step)
                || CUPurapConstants.IWantDocumentSteps.ITEMS_AND_ACCT_DATA_STEP.equalsIgnoreCase(step)
                || CUPurapConstants.IWantDocumentSteps.VENDOR_STEP.equalsIgnoreCase(step)
                || CUPurapConstants.IWantDocumentSteps.ROUTING_STEP.equalsIgnoreCase(step)) {
            return step;
        } else {
            return CUPurapConstants.IWantDocumentSteps.REGULAR;
        }
    }

    public void setStep(String step) {
        this.step = step;
    }
    
    /**
     * Returns the new ad hoc route person's netId surrounded by wildcards,
     * or a blank value if the ad hoc route person is null or has a blank netId.
     */
    public String getNewAdHocRoutePersonIdForLookup() {
        if (getNewAdHocRoutePerson() != null) {
            if (StringUtils.isNotBlank(getNewAdHocRoutePerson().getId())) {
                return KFSConstants.WILDCARD_CHARACTER + getNewAdHocRoutePerson().getId()
                        + KFSConstants.WILDCARD_CHARACTER;
            }
        }
        return StringUtils.EMPTY;
    }
    
    public String getNewAdHocRoutePersonNetId() {
        if (getNewAdHocRoutePerson() != null) {
            if (StringUtils.isNotBlank(getNewAdHocRoutePerson().getId())) {
                return getNewAdHocRoutePerson().getId();
            }
        }
        return StringUtils.EMPTY;
    }

    /**
     * This overridden method ...
     * 
     * @see org.kuali.kfs.sys.document.web.struts.FinancialSystemTransactionalDocumentFormBase#getExtraButtons()
     */
    public List<ExtraButton> getExtraButtons() {
        extraButtons.clear();
        
        String wizard = (String) getEditingMode().get("wizard");

        String customerDataStep = (String) getEditingMode().get(CUPurapConstants.IWantDocumentSteps.CUSTOMER_DATA_STEP);
        String itemsAndAcctStep = (String) getEditingMode().get(
                CUPurapConstants.IWantDocumentSteps.ITEMS_AND_ACCT_DATA_STEP);
        String vendorDataStep = (String) getEditingMode().get(CUPurapConstants.IWantDocumentSteps.VENDOR_STEP);
        String routingStep = (String) getEditingMode().get(CUPurapConstants.IWantDocumentSteps.ROUTING_STEP);
        
        boolean isFullPageAllowed = Boolean.parseBoolean((String) getEditingMode().get(CUPurapConstants.I_WANT_DOC_FULL_PAGE_IS_ALLOWED));
        boolean isMultiplePagesAllowed = Boolean.parseBoolean((String) getEditingMode().get(CUPurapConstants.I_WANT_DOC_MULTIPLE_PAGE_IS_ALLOWED));
        
        if (getEditingMode().containsKey(CUPurapConstants.IWNT_DOC_DISPLAY_CONFIRMATION)) {
            extraButtons.add(createConfirmYesButton());
            extraButtons.add(createConfirmNoButton());
        } else {

            if (ObjectUtils.isNotNull(customerDataStep) && wizard.equalsIgnoreCase(customerDataStep)) {
                extraButtons.add(createContinueToItemsButton());

            } else if (ObjectUtils.isNotNull(itemsAndAcctStep) && wizard.equalsIgnoreCase(itemsAndAcctStep)) {

                extraButtons.add(createBackToCustomerDataButton());
                extraButtons.add(createContinueToVendorButton());

            } else if (ObjectUtils.isNotNull(vendorDataStep) && wizard.equalsIgnoreCase(vendorDataStep)) {

                extraButtons.add(createBackToItemsButton());
                extraButtons.add(createContinueToRoutingButton());

            } else if (ObjectUtils.isNotNull(routingStep) && wizard.equalsIgnoreCase(routingStep)) {

                extraButtons.add(createBackToVendorButton());
                extraButtons.add(createSubmitButton());

            }

            if (getEditingMode().containsKey(CUPurapConstants.IWNT_DOC_CREATE_REQ)) {
                extraButtons.add(createCreateRequisitionButton());
            }

            if (getEditingMode().containsKey(CUPurapConstants.IWNT_DOC_CREATE_DV)) {
                // KFSPTS-2527 add create DV button
                extraButtons.add(createCreateDVButton());
            }
            
            if (getEditingMode().containsKey(CUPurapConstants.IWNT_DOC_RETURN_TO_SSC)) {
                extraButtons.add(createReturnToSSCButton());
            }

            createAppropriatePresentationModeChangeButton(isFullPageAllowed, isMultiplePagesAllowed);
        }

        return extraButtons;
    }
    
    private void createAppropriatePresentationModeChangeButton(boolean isFullPageAllowed, boolean isMultiplePagesAllowed) {
        if (StringUtils.equalsIgnoreCase(getPresentationMode(), CUPurapConstants.IWantPresentationModes.MULTIPLE_PAGE_MODE) && isFullPageAllowed) {
            extraButtons.add(createSwitchToFullPagePresentationButton());
        } else if (StringUtils.equalsIgnoreCase(getPresentationMode(), CUPurapConstants.IWantPresentationModes.FULL_PAGE_MODE) && isMultiplePagesAllowed) {
            extraButtons.add(createSwitchToMultipleStepsPresentationButton());
        }
    }
    
    protected ExtraButton createSwitchToFullPagePresentationButton() {
        ExtraButton switchToFullPagePresentationButton = new ExtraButton();
        switchToFullPagePresentationButton.setExtraButtonProperty("methodToCall.switchToFullPagePresentation");
        switchToFullPagePresentationButton.setExtraButtonSource("${" + KFSConstants.EXTERNALIZABLE_IMAGES_URL_KEY + "}buttonsmall_continue.gif");
        switchToFullPagePresentationButton.setExtraButtonAltText("Show 1 Page");
        return switchToFullPagePresentationButton;
    }
    
    protected ExtraButton createSwitchToMultipleStepsPresentationButton() {
        ExtraButton switchToMultipleStepsPresentationButton = new ExtraButton();
        switchToMultipleStepsPresentationButton.setExtraButtonProperty("methodToCall.switchToMultipleStepsPresentation");
        switchToMultipleStepsPresentationButton.setExtraButtonSource("${" + KFSConstants.EXTERNALIZABLE_IMAGES_URL_KEY + "}buttonsmall_continue.gif");
        switchToMultipleStepsPresentationButton.setExtraButtonAltText("Show 4 Steps");
        return switchToMultipleStepsPresentationButton;
    }
    
    protected ExtraButton createReturnToSSCButton() {
        ExtraButton returnToSSCButton = new ExtraButton();
        returnToSSCButton.setExtraButtonProperty("methodToCall.returnToSSC");
        returnToSSCButton.setExtraButtonSource("${" + KFSConstants.EXTERNALIZABLE_IMAGES_URL_KEY + "}buttonsmall_continue.gif");
        returnToSSCButton.setExtraButtonAltText("Return To Shared Service Center");
        returnToSSCButton.setExtraButtonOnclick("resetScrollPosition();");
        return returnToSSCButton;
    }

    /**
     * Creates the continue button on the customer data page that points to the items page
     * 
     * @return
     */
    protected ExtraButton createContinueToItemsButton() {
        ExtraButton continueButton = new ExtraButton();
        continueButton.setExtraButtonProperty("methodToCall.continueToItems");
        continueButton.setExtraButtonSource("${" + KFSConstants.EXTERNALIZABLE_IMAGES_URL_KEY
                + "}buttonsmall_continue.gif");
        continueButton.setExtraButtonAltText("Continue");
        return continueButton;
    }

    /**
     * Creates the continue button on the items page that points to the vendor page
     * 
     * @return
     */
    protected ExtraButton createContinueToVendorButton() {
        ExtraButton clearButton = new ExtraButton();
        clearButton.setExtraButtonProperty("methodToCall.continueToVendor");
        clearButton.setExtraButtonSource("${" + KFSConstants.EXTERNALIZABLE_IMAGES_URL_KEY
                + "}buttonsmall_continue.gif");
        clearButton.setExtraButtonAltText("Continue");
        return clearButton;
    }

    /**
     * Creates the back button on the items page that points to the cutsomer data page
     * 
     * @return
     */
    protected ExtraButton createBackToCustomerDataButton() {
        ExtraButton clearButton = new ExtraButton();
        clearButton.setExtraButtonProperty("methodToCall.continueToCustomerData");
        clearButton.setExtraButtonSource("${" + KFSConstants.EXTERNALIZABLE_IMAGES_URL_KEY + "}buttonsmall_back.gif");
        clearButton.setExtraButtonAltText("Back");
        return clearButton;
    }

    /**
     * Creates the continue button on the vendor page that points to the routing page
     * 
     * @return
     */
    protected ExtraButton createContinueToRoutingButton() {
        ExtraButton printButton = new ExtraButton();
        printButton.setExtraButtonProperty("methodToCall.continueToRouting");
        printButton.setExtraButtonSource("${" + KFSConstants.EXTERNALIZABLE_IMAGES_URL_KEY
                + "}buttonsmall_continue.gif");
        printButton.setExtraButtonAltText("Continue");
        return printButton;
    }

    /**
     * Creates the back button on the vendor page that points to the items page
     * 
     * @return
     */
    protected ExtraButton createBackToItemsButton() {
        ExtraButton clearButton = new ExtraButton();
        clearButton.setExtraButtonProperty("methodToCall.continueToItems");
        clearButton.setExtraButtonSource("${" + KFSConstants.EXTERNALIZABLE_IMAGES_URL_KEY + "}buttonsmall_back.gif");
        clearButton.setExtraButtonAltText("Back");
        return clearButton;
    }

    /**
     * Creates the back button on the routing page that points to the vendor page
     * 
     * @return
     */
    protected ExtraButton createBackToVendorButton() {
        ExtraButton clearButton = new ExtraButton();
        clearButton.setExtraButtonProperty("methodToCall.continueToVendor");
        clearButton.setExtraButtonSource("${" + KFSConstants.EXTERNALIZABLE_IMAGES_URL_KEY + "}buttonsmall_back.gif");
        clearButton.setExtraButtonAltText("Back");
        return clearButton;
    }

    /**
     * Creates the submit button on the routing page
     * 
     * @return
     */
    protected ExtraButton createSubmitButton() {
        ExtraButton clearButton = new ExtraButton();
        clearButton.setExtraButtonProperty("methodToCall.route");
        clearButton.setExtraButtonSource("${" + KFSConstants.EXTERNALIZABLE_IMAGES_URL_KEY
                + "}buttonsmall_submit.gif");
        clearButton.setExtraButtonAltText("Submit");
        return clearButton;
    }

    /**
     * Creates the button for creating the requisition. If JavaScript is enabled on the client,
     * then the requisition will be created in a new window/tab instead of the current one.
     * 
     * @return
     */
    protected ExtraButton createCreateRequisitionButton() {
        ExtraButton createReqButton = new ExtraButton();
        createReqButton.setExtraButtonProperty("methodToCall.createRequisition");
        createReqButton.setExtraButtonSource("${" + KFSConstants.EXTERNALIZABLE_IMAGES_URL_KEY + "}buttonsmall_create_req.gif");
        createReqButton.setExtraButtonAltText("Create Req");
        if (getEditingMode().containsKey(CUPurapConstants.IWNT_DOC_EDIT_CONTRACT_INDICATOR) && isInOrgHierarchyNode(getDocument())) {
            createReqButton.setExtraButtonOnclick(
                    " if((document.getElementsByName('document.contractIndicator'))[1].checked) " 
                            + " { " 
                            + " if(confirm('" + getContractWarningMessage(CUPurapConstants.IWantDocumentRelatedDocsLabels.REQUISITION) + "')){ " 
                            + "window.open('"
                            + ConfigContext.getCurrentContextConfig().getProperty(KFSConstants.APPLICATION_URL_KEY)
                            + "/purapRequisition.do?methodToCall=createReqFromIWantDoc&docId="
                            + getDocument().getDocumentNumber() 
                            + "'); return false;" 
                            + " } " 
                            + " else { " 
                            + "return false; "
                            + " } } "
                            + " else {"
                            + "window.open('" 
                            + ConfigContext.getCurrentContextConfig().getProperty(KFSConstants.APPLICATION_URL_KEY)
                            + "/purapRequisition.do?methodToCall=createReqFromIWantDoc&docId=" 
                            + getDocument().getDocumentNumber()
                            + "'); return false; "
                            + " } "
                    );
        } else if (!getEditingMode().containsKey(CUPurapConstants.IWNT_DOC_EDIT_CONTRACT_INDICATOR)
                        && isInOrgHierarchyNode(getDocument()) && KRADConstants.YES_INDICATOR_VALUE.equalsIgnoreCase(getIWantDocument().getContractIndicator())) {
            createReqButton.setExtraButtonOnclick(
                            " if (confirm('" + getContractWarningMessage(CUPurapConstants.IWantDocumentRelatedDocsLabels.REQUISITION) + "')) { " 
                            + "window.open('"
                            + ConfigContext.getCurrentContextConfig().getProperty(KFSConstants.APPLICATION_URL_KEY)
                            + "/purapRequisition.do?methodToCall=createReqFromIWantDoc&docId="
                            + getDocument().getDocumentNumber() 
                            + "'); return false;" 
                            + " } " 
                            + " else { " 
                            + "return false; "
                            + " } "
                    );
        } else {
            createReqButton.setExtraButtonOnclick("window.open('" + ConfigContext.getCurrentContextConfig().getProperty(KFSConstants.APPLICATION_URL_KEY)
                    + "/purapRequisition.do?methodToCall=createReqFromIWantDoc&docId=" + getDocument().getDocumentNumber()
                    + "');return false;");
        }

        return createReqButton;
    }
    
    protected ExtraButton createCreateDVButton() {
        ExtraButton createDVButton = new ExtraButton();
        createDVButton.setExtraButtonProperty("methodToCall.createDV");
        createDVButton.setExtraButtonSource("${" + KFSConstants.EXTERNALIZABLE_IMAGES_URL_KEY + "}buttonsmall_create_DV.gif");
        createDVButton.setExtraButtonAltText("Create DV");
        if (getEditingMode().containsKey(CUPurapConstants.IWNT_DOC_EDIT_CONTRACT_INDICATOR) && isInOrgHierarchyNode(getDocument())) {
            createDVButton.setExtraButtonOnclick(
                    " if((document.getElementsByName('document.contractIndicator'))[1].checked) " 
                            + " { " 
                            + " if(confirm('" + getContractWarningMessage(CUPurapConstants.IWantDocumentRelatedDocsLabels.DV) + "')){ " 
                            + "window.open('"
                            + ConfigContext.getCurrentContextConfig().getProperty(KFSConstants.APPLICATION_URL_KEY)
                            + "/financialDisbursementVoucher.do?methodToCall=createDVFromIWantDoc&docId="
                            + getDocument().getDocumentNumber() 
                            + "'); return false;" 
                            + " } " 
                            + " else { " 
                            + "return false; "
                            + " } } "
                            + " else {"
                            + "window.open('" 
                            + ConfigContext.getCurrentContextConfig().getProperty(KFSConstants.APPLICATION_URL_KEY)
                            + "/financialDisbursementVoucher.do?methodToCall=createDVFromIWantDoc&docId=" 
                            + getDocument().getDocumentNumber()
                            + "'); return false; "
                            + " } "
                    );
        } else if (!getEditingMode().containsKey(CUPurapConstants.IWNT_DOC_EDIT_CONTRACT_INDICATOR)
                        && isInOrgHierarchyNode(getDocument()) && KRADConstants.YES_INDICATOR_VALUE.equalsIgnoreCase(getIWantDocument().getContractIndicator())) {
            createDVButton.setExtraButtonOnclick(
                            " if (confirm('" + getContractWarningMessage(CUPurapConstants.IWantDocumentRelatedDocsLabels.DV) + "')) { " 
                            + "window.open('"
                            + ConfigContext.getCurrentContextConfig().getProperty(KFSConstants.APPLICATION_URL_KEY)
                            + "/financialDisbursementVoucher.do?methodToCall=createDVFromIWantDoc&docId="
                            + getDocument().getDocumentNumber() 
                            + "'); return false;" 
                            + " } " 
                            + " else { " 
                            + "return false; "
                            + " } "
                    );
        } else {
            createDVButton.setExtraButtonOnclick("window.open('" + ConfigContext.getCurrentContextConfig().getProperty(KFSConstants.APPLICATION_URL_KEY)
                    + "/financialDisbursementVoucher.do?methodToCall=createDVFromIWantDoc&docId=" + getDocument().getDocumentNumber()
                    + "');return false;");
        }

        return createDVButton;
    }
    
    protected ExtraButton createConfirmYesButton() {
        ExtraButton confirmYesButton = new ExtraButton();
        confirmYesButton.setExtraButtonProperty("methodToCall.createRequisition");
        confirmYesButton.setExtraButtonSource("${" + KFSConstants.EXTERNALIZABLE_IMAGES_URL_KEY + "}buttonsmall_yes.gif");
        confirmYesButton.setExtraButtonAltText("Yes");
        return confirmYesButton;
    }
    
    protected ExtraButton createConfirmNoButton() {
        ExtraButton confirmNoButton = new ExtraButton();
        confirmNoButton.setExtraButtonProperty("methodToCall.cancelCreateReq");
        confirmNoButton.setExtraButtonSource("${" + KFSConstants.EXTERNALIZABLE_IMAGES_URL_KEY + "}buttonsmall_no.gif");
        confirmNoButton.setExtraButtonAltText("No");
        return confirmNoButton;
    }
    
    public boolean isDocEnroute() {
        final WorkflowDocument workflowDocument = getDocument().getDocumentHeader().getWorkflowDocument();
        return ObjectUtils.isNotNull(workflowDocument) && workflowDocument.isEnroute();
    }
    
    public String getContractWarningMessage(String docType) {
        return MessageFormat.format(getConfigurationService()
                .getPropertyValueAsString(CUPurapKeyConstants.MESSAGE_IWNT_CONFIRM_CREATE_REQ_OR_DV), docType);
    }
    
    public ConfigurationService getConfigurationService() {
         return SpringContext.getBean(ConfigurationService.class);
    }

    public String getPresentationMode() {
        return presentationMode;
    }

    public void setPresentationMode(String presentationMode) {
        this.presentationMode = presentationMode;
    }

    public void setUserMatchesInitiator(boolean userMatchesInitiator) {
        this.userMatchesInitiator = userMatchesInitiator;
    }

    public boolean isUserMatchesInitiator() {
        return userMatchesInitiator;
    }

    public boolean isDocIsInitiatedOrSaved() {
        return docIsInitiatedOrSaved;
    }

    public void setDocIsInitiatedOrSaved(boolean docIsInitiatedOrSaved) {
        this.docIsInitiatedOrSaved = docIsInitiatedOrSaved;
    }
    
    private boolean isDocInNode(Document document, String node) {
        WorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
        Set<String> nodeNames = workflowDocument.getCurrentNodeNames();
        
        if (CollectionUtils.isNotEmpty(nodeNames)) {
            return nodeNames.contains(node);
        }
        return false;
    }
    
    private boolean isInOrgHierarchyNode(Document document) {
        return isDocInNode(document, KFSConstants.RouteLevelNames.ORGANIZATION_HIERARCHY);
    }
    
}
