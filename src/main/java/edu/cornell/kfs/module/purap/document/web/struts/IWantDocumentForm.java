package edu.cornell.kfs.module.purap.document.web.struts;

import edu.cornell.kfs.module.purap.CUPurapConstants;
import edu.cornell.kfs.module.purap.businessobject.IWantAccount;
import edu.cornell.kfs.module.purap.businessobject.IWantItem;
import edu.cornell.kfs.module.purap.document.IWantDocument;
import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.kns.web.ui.ExtraButton;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.document.web.struts.FinancialSystemTransactionalDocumentFormBase;
import org.kuali.rice.core.api.config.property.ConfigContext;
import org.kuali.rice.core.api.util.KeyValue;
import org.kuali.rice.kew.api.KewApiConstants.SearchableAttributeConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("deprecation")
public class IWantDocumentForm extends FinancialSystemTransactionalDocumentFormBase {

    private static final long serialVersionUID = -82175061546434849L;

    protected boolean isWizard;
    protected String step;
    protected String headerTitle;

    protected IWantItem newIWantItemLine;
    protected IWantAccount newSourceLine;

    protected List<KeyValue> deptOrgKeyLabels;
    protected String previousSelectedOrg = KFSConstants.EMPTY_STRING;

    public IWantDocumentForm() {
        super();
        setNewIWantItemLine(new IWantItem());
        newSourceLine = new IWantAccount();
        this.setDeptOrgKeyLabels(new ArrayList<KeyValue>());
        editingMode = new HashMap<Object,Object>();

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
        //return SpringContext.getBean(KualiConfigurationService.class).getPropertyString(KFSConstants.EXTERNALIZABLE_HELP_URL_KEY) +
        //SpringContext.getBean(ParameterService.class).getParameterValue(
        //KfsParameterConstants.PURCHASING_DOCUMENT.class, PurapParameterConstants.LINE_ITEM_IMPORT);
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
                return SearchableAttributeConstants.SEARCH_WILDCARD_CHARACTER + getNewAdHocRoutePerson().getId()
                        + SearchableAttributeConstants.SEARCH_WILDCARD_CHARACTER;
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

        if (ObjectUtils.isNotNull(customerDataStep) && wizard.equalsIgnoreCase(customerDataStep)) {
            extraButtons.add(createContinueToItemsButton());
            // extraButtons.add(createClearInitFieldsButton());
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
        
        if(getEditingMode().containsKey(CUPurapConstants.IWNT_DOC_CREATE_DV)){
            //KFSPTS-2527 add create DV button
            extraButtons.add(createCreateDVButton());
        }
        
        return extraButtons;
    }

    /**
     * Creates the continue button on the customer data page that points to the items page
     * 
     * @return
     */
    protected ExtraButton createContinueToItemsButton() {
        ExtraButton continueButton = new ExtraButton();
        continueButton.setExtraButtonProperty("methodToCall.continueToItems");
        continueButton.setExtraButtonSource("${" + KFSConstants.RICE_EXTERNALIZABLE_IMAGES_URL_KEY
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
        clearButton.setExtraButtonSource("${" + KFSConstants.RICE_EXTERNALIZABLE_IMAGES_URL_KEY
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
        printButton.setExtraButtonSource("${" + KFSConstants.RICE_EXTERNALIZABLE_IMAGES_URL_KEY
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
        clearButton.setExtraButtonSource("${" + KFSConstants.RICE_EXTERNALIZABLE_IMAGES_URL_KEY
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
        ExtraButton clearButton = new ExtraButton();
        clearButton.setExtraButtonProperty("methodToCall.createRequisition");
        clearButton.setExtraButtonSource("${" + KFSConstants.EXTERNALIZABLE_IMAGES_URL_KEY + "}buttonsmall_create_req.gif");
        clearButton.setExtraButtonAltText("Create Req");
        clearButton.setExtraButtonOnclick("window.open('" + ConfigContext.getCurrentContextConfig().getProperty(KRADConstants.APPLICATION_URL_KEY)
                + "/purapRequisition.do?methodToCall=createReqFromIWantDoc&docId=" + getDocument().getDocumentNumber()
                + "');return false;");
        return clearButton;
    }
    
    protected ExtraButton createCreateDVButton() {
        ExtraButton clearButton = new ExtraButton();
        clearButton.setExtraButtonProperty("methodToCall.createDV");
        clearButton.setExtraButtonSource("${" + KFSConstants.EXTERNALIZABLE_IMAGES_URL_KEY + "}buttonsmall_create_DV.gif");
        clearButton.setExtraButtonAltText("Create DV");
        clearButton.setExtraButtonOnclick("window.open('" + ConfigContext.getCurrentContextConfig().getProperty(KRADConstants.APPLICATION_URL_KEY)
                + "/financialDisbursementVoucher.do?methodToCall=createDVFromIWantDoc&docId=" + getDocument().getDocumentNumber()
                + "');return false;");
        clearButton.setExtraButtonParams("_blank");
        return clearButton;
    }

}
