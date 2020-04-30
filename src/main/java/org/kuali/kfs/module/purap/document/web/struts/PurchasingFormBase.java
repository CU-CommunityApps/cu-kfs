/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2020 Kuali, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.kfs.module.purap.document.web.struts;

import org.apache.struts.upload.FormFile;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.integration.purap.CapitalAssetLocation;
import org.kuali.kfs.kns.web.ui.ExtraButton;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurapParameterConstants;
import org.kuali.kfs.module.purap.PurapPropertyConstants;
import org.kuali.kfs.module.purap.businessobject.PurApAccountingLine;
import org.kuali.kfs.module.purap.businessobject.PurApAccountingLineBase;
import org.kuali.kfs.module.purap.businessobject.PurApItem;
import org.kuali.kfs.module.purap.document.PurchasingDocument;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.rice.kew.api.WorkflowDocument;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Struts Action Form for Purchasing documents.
 */
public abstract class PurchasingFormBase extends PurchasingAccountsPayableFormBase {

    protected Boolean notOtherDeliveryBuilding = true;
    protected Boolean hideDistributeAccounts = true;
    protected PurApItem newPurchasingItemLine;
    protected FormFile itemImportFile; // file from which items can be imported
    protected String distributePurchasingCommodityCode;
    protected String distributePurchasingCommodityDescription;
    protected boolean calculated;

    protected String initialZipCode;

    // *** Note that the following variables do not use camel caps ON PURPOSE, because of how the accounting lines tag uses the
    // accountPrefix
    protected Integer accountDistributionnextSourceLineNumber;
    protected List<PurApAccountingLine> accountDistributionsourceAccountingLines;
    protected PurApAccountingLine accountDistributionnewSourceLine;

    protected CapitalAssetLocation newPurchasingCapitalAssetLocationLine;

    protected BigDecimal totalPercentageOfAccountDistributionsourceAccountingLines;

    protected String locationBuildingFromLookup;
    protected String locationCampusFromLookup;
    
    // KFSPTS-794 related
    protected String reasonToChange;

    /**
     * Constructs a PurchasingFormBase instance and sets up the appropriately casted document.
     */
    public PurchasingFormBase() {
        super();
        this.setNewPurchasingItemLine(setupNewPurchasingItemLine());
        newPurchasingItemLine.setItemTypeCode("ITEM");

        this.accountDistributionnextSourceLineNumber = new Integer(1);
        setAccountDistributionsourceAccountingLines(new ArrayList());
        this.setAccountDistributionnewSourceLine(setupNewAccountDistributionAccountingLine());

        this.setNewPurchasingCapitalAssetLocationLine(this.setupNewPurchasingCapitalAssetLocationLine());

        calculated = false;
    }


    @Override
    protected void populateAccountingLinesForResponse(String methodToCall, Map parameterMap) {
        super.populateAccountingLinesForResponse(methodToCall, parameterMap);
        populateAccountingLine(getAccountDistributionnewSourceLine(), PurapPropertyConstants.ACCOUNT_DISTRIBUTION_NEW_SRC_LINE, parameterMap);

    }
    
    public Boolean getNotOtherDeliveryBuilding() {
        return notOtherDeliveryBuilding;
    }

    public void setNotOtherDeliveryBuilding(Boolean notOtherDeliveryBuilding) {
        this.notOtherDeliveryBuilding = notOtherDeliveryBuilding;
    }

    public Boolean getHideDistributeAccounts() {
        return hideDistributeAccounts;
    }

    public void setHideDistributeAccounts(Boolean hideDistributeAccounts) {
        this.hideDistributeAccounts = hideDistributeAccounts;
    }

    public Integer getAccountDistributionnextSourceLineNumber() {
        return accountDistributionnextSourceLineNumber;
    }

    public void setAccountDistributionnextSourceLineNumber(Integer accountDistributionnextSourceLineNumber) {
        this.accountDistributionnextSourceLineNumber = accountDistributionnextSourceLineNumber;
    }

    public List<PurApAccountingLine> getAccountDistributionsourceAccountingLines() {
        return accountDistributionsourceAccountingLines;
    }

    public void setAccountDistributionsourceAccountingLines(List<PurApAccountingLine> accountDistributionAccountingLines) {
        this.accountDistributionsourceAccountingLines = accountDistributionAccountingLines;
    }

    public BigDecimal getTotalPercentageOfAccountDistributionsourceAccountingLines() {
        this.totalPercentageOfAccountDistributionsourceAccountingLines = new BigDecimal(0);
        for (PurApAccountingLine line : this.getAccountDistributionsourceAccountingLines()) {
            if (line.getAccountLinePercent() != null) {
                setTotalPercentageOfAccountDistributionsourceAccountingLines(this.totalPercentageOfAccountDistributionsourceAccountingLines.add(line.getAccountLinePercent()));
            }
        }
        return this.totalPercentageOfAccountDistributionsourceAccountingLines;
    }

    public void setTotalPercentageOfAccountDistributionsourceAccountingLines(BigDecimal total) {
        this.totalPercentageOfAccountDistributionsourceAccountingLines = total;
    }

    public PurApAccountingLine getAccountDistributionnewSourceLine() {
        return accountDistributionnewSourceLine;
    }

    public void setAccountDistributionnewSourceLine(PurApAccountingLine accountDistributionnewSourceLine) {
        this.accountDistributionnewSourceLine = accountDistributionnewSourceLine;
    }

    public PurApItem getNewPurchasingItemLine() {
        return newPurchasingItemLine;
    }

    public void setNewPurchasingItemLine(PurApItem newPurchasingItemLine) {
        this.newPurchasingItemLine = newPurchasingItemLine;
    }

    public FormFile getItemImportFile() {
        return itemImportFile;
    }

    public void setItemImportFile(FormFile itemImportFile) {
        this.itemImportFile = itemImportFile;
    }

    /**
     * Returns the Account Distribution Source Accounting Line at the specified index.
     *
     * @param index the index of the Account Distribution Source Accounting Line.
     * @return the specified Account Distribution Source Accounting Line.
     */
    public PurApAccountingLine getAccountDistributionsourceAccountingLine(int index) {
        while (accountDistributionsourceAccountingLines.size() <= index) {
            accountDistributionsourceAccountingLines.add(setupNewAccountDistributionAccountingLine());
        }
        return accountDistributionsourceAccountingLines.get(index);
    }

    /**
     * Returns the new Purchasing Item Line and resets it to null.
     *
     * @return the new Purchasing Item Line.
     */
    public PurApItem getAndResetNewPurchasingItemLine() {
        PurApItem aPurchasingItemLine = getNewPurchasingItemLine();
        setNewPurchasingItemLine(setupNewPurchasingItemLine());
        return aPurchasingItemLine;
    }

    /**
     * This method should be overriden (or see accountingLines for an alternate way of doing this with newInstance)
     */
    public PurApItem setupNewPurchasingItemLine() {
        return null;
    }

    /**
     * This method should be overriden (or see accountingLines for an alternate way of doing this with newInstance)
     */
    public PurApAccountingLineBase setupNewPurchasingAccountingLine() {
        return null;
    }

    /**
     * This method should be overriden.
     */
    public PurApAccountingLineBase setupNewAccountDistributionAccountingLine() {
        return null;
    }

    /**
     * Sets the sequence number appropriately for the passed in source accounting line using the value that has been stored in the
     * nextSourceLineNumber variable, adds the accounting line to the list that is aggregated by this object, and then handles
     * incrementing the nextSourceLineNumber variable.
     *
     * @param line the accounting line to add to the list.
     * @see org.kuali.kfs.sys.document.AccountingDocument#addSourceAccountingLine(SourceAccountingLine)
     */
    public void addAccountDistributionsourceAccountingLine(PurApAccountingLine line) {
        line.setSequenceNumber(this.getAccountDistributionnextSourceLineNumber());
        this.accountDistributionsourceAccountingLines.add(line);
        this.accountDistributionnextSourceLineNumber = new Integer(this.getAccountDistributionnextSourceLineNumber().intValue() + 1);
        this.setAccountDistributionnewSourceLine(setupNewAccountDistributionAccountingLine());
    }

    public String getDistributePurchasingCommodityCode() {
        return distributePurchasingCommodityCode;
    }

    public void setDistributePurchasingCommodityCode(String distributePurchasingCommodityCode) {
        this.distributePurchasingCommodityCode = distributePurchasingCommodityCode;
    }

    public String getDistributePurchasingCommodityDescription() {
        return distributePurchasingCommodityDescription;
    }

    public void setDistributePurchasingCommodityDescription(String distributePurchasingCommodityDescription) {
        this.distributePurchasingCommodityDescription = distributePurchasingCommodityDescription;
    }

    public Class getItemCapitalAssetClass(){
        return null;
    }

    public Class getCapitalAssetLocationClass(){
        return null;
    }

    //CAMS LOCATION
    //Must be overridden
    public CapitalAssetLocation setupNewPurchasingCapitalAssetLocationLine() {
        CapitalAssetLocation location = null;
        try{
            location = (CapitalAssetLocation)getCapitalAssetLocationClass().newInstance();
        }
        catch (InstantiationException e) {
            throw new RuntimeException("Unable to get class");
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException("Unable to get class");
        }
        catch (NullPointerException e) {
            throw new RuntimeException("Can't instantiate Purchasing Account from base");
        }

        return location;
    }

    public void setNewPurchasingCapitalAssetLocationLine(CapitalAssetLocation newCapitalAssetLocationLine) {
        this.newPurchasingCapitalAssetLocationLine = newCapitalAssetLocationLine;
    }

    public CapitalAssetLocation getNewPurchasingCapitalAssetLocationLine() {
        return newPurchasingCapitalAssetLocationLine;
    }

    public CapitalAssetLocation getAndResetNewPurchasingCapitalAssetLocationLine() {
        CapitalAssetLocation assetLocation = getNewPurchasingCapitalAssetLocationLine();
        setNewPurchasingCapitalAssetLocationLine(setupNewPurchasingCapitalAssetLocationLine());
        return assetLocation;
    }

    public void resetNewPurchasingCapitalAssetLocationLine() {
        setNewPurchasingCapitalAssetLocationLine(setupNewPurchasingCapitalAssetLocationLine());
    }

    //Availability once
    public String getPurchasingItemCapitalAssetAvailability(){
        String availability = PurapConstants.CapitalAssetAvailability.NONE;
        PurchasingDocument pd = (PurchasingDocument)this.getDocument();

        if( (PurapConstants.CapitalAssetSystemTypes.ONE_SYSTEM.equals(pd.getCapitalAssetSystemTypeCode()) && PurapConstants.CapitalAssetSystemStates.MODIFY.equals(pd.getCapitalAssetSystemStateCode())) ||
            (PurapConstants.CapitalAssetSystemTypes.MULTIPLE.equals(pd.getCapitalAssetSystemTypeCode()) && PurapConstants.CapitalAssetSystemStates.MODIFY.equals(pd.getCapitalAssetSystemStateCode())) ){

            availability = PurapConstants.CapitalAssetAvailability.ONCE;

        }else if((PurapConstants.CapitalAssetSystemTypes.INDIVIDUAL.equals(pd.getCapitalAssetSystemTypeCode()) && PurapConstants.CapitalAssetSystemStates.MODIFY.equals(pd.getCapitalAssetSystemStateCode()))){

            availability = PurapConstants.CapitalAssetAvailability.EACH;

        }

        return availability;
    }

    public String getPurchasingCapitalAssetSystemAvailability(){
        String availability = PurapConstants.CapitalAssetAvailability.NONE;
        PurchasingDocument pd = (PurchasingDocument)this.getDocument();

        if( (PurapConstants.CapitalAssetSystemTypes.ONE_SYSTEM.equals(pd.getCapitalAssetSystemTypeCode()) && PurapConstants.CapitalAssetSystemStates.NEW.equals(pd.getCapitalAssetSystemStateCode())) ){

            availability = PurapConstants.CapitalAssetAvailability.ONCE;

        }else if((PurapConstants.CapitalAssetSystemTypes.INDIVIDUAL.equals(pd.getCapitalAssetSystemTypeCode()) && PurapConstants.CapitalAssetSystemStates.NEW.equals(pd.getCapitalAssetSystemStateCode()))){

            availability = PurapConstants.CapitalAssetAvailability.EACH;

        }

        return availability;
    }

    public String getPurchasingCapitalAssetSystemCommentsAvailability(){
        String availability = PurapConstants.CapitalAssetAvailability.NONE;
        PurchasingDocument pd = (PurchasingDocument)this.getDocument();

        if( (PurapConstants.CapitalAssetSystemTypes.ONE_SYSTEM.equals(pd.getCapitalAssetSystemTypeCode()) || PurapConstants.CapitalAssetSystemTypes.MULTIPLE.equals(pd.getCapitalAssetSystemTypeCode())) ){

            availability = PurapConstants.CapitalAssetAvailability.ONCE;

        }else if(PurapConstants.CapitalAssetSystemTypes.INDIVIDUAL.equals(pd.getCapitalAssetSystemTypeCode()) ){

            availability = PurapConstants.CapitalAssetAvailability.EACH;

        }

        return availability;
    }

    public String getPurchasingCapitalAssetSystemDescriptionAvailability(){
        String availability = PurapConstants.CapitalAssetAvailability.NONE;
        PurchasingDocument pd = (PurchasingDocument)this.getDocument();

        if( (PurapConstants.CapitalAssetSystemTypes.ONE_SYSTEM.equals(pd.getCapitalAssetSystemTypeCode()) && PurapConstants.CapitalAssetSystemStates.NEW.equals(pd.getCapitalAssetSystemStateCode())) ||
            (PurapConstants.CapitalAssetSystemTypes.MULTIPLE.equals(pd.getCapitalAssetSystemTypeCode()) && PurapConstants.CapitalAssetSystemStates.NEW.equals(pd.getCapitalAssetSystemStateCode())) ){

            availability = PurapConstants.CapitalAssetAvailability.ONCE;

        }

        return availability;
    }

    public String getPurchasingCapitalAssetLocationAvailability() {
        String availability = PurapConstants.CapitalAssetAvailability.NONE;
        PurchasingDocument pd = (PurchasingDocument) this.getDocument();

        if ((PurapConstants.CapitalAssetSystemTypes.ONE_SYSTEM.equals(pd.getCapitalAssetSystemTypeCode()) && PurapConstants.CapitalAssetSystemStates.NEW.equals(pd.getCapitalAssetSystemStateCode()))) {
            availability = PurapConstants.CapitalAssetAvailability.ONCE;
        }
        else if ((PurapConstants.CapitalAssetSystemTypes.INDIVIDUAL.equals(pd.getCapitalAssetSystemTypeCode()) && PurapConstants.CapitalAssetSystemStates.NEW.equals(pd.getCapitalAssetSystemStateCode()))) {
            availability = PurapConstants.CapitalAssetAvailability.EACH;
        }

        return availability;
    }

    public String getPurchasingCapitalAssetCountAssetNumberAvailability() {
        String availability = PurapConstants.CapitalAssetAvailability.NONE;
        PurchasingDocument pd = (PurchasingDocument) this.getDocument();

        if ((PurapConstants.CapitalAssetSystemTypes.ONE_SYSTEM.equals(pd.getCapitalAssetSystemTypeCode()) && PurapConstants.CapitalAssetSystemStates.NEW.equals(pd.getCapitalAssetSystemStateCode()))) {
            availability = PurapConstants.CapitalAssetAvailability.ONCE;
        }

        return availability;
    }

    /**
    * KRAD Conversion: Performs customization of an extra buttons.
     * <p>
    * No data dictionary is involved.
    */

    @Override
    public List<ExtraButton> getExtraButtons() {
        extraButtons.clear();

        String appExternalImageURL = SpringContext.getBean(ConfigurationService.class).getPropertyValueAsString(KFSConstants.EXTERNALIZABLE_IMAGES_URL_KEY);

        // add the calculate button if the user can edit
        if (canUserCalculate()) {
            addExtraButton("methodToCall.calculate", appExternalImageURL + "buttonsmall_calculate.gif", "Calculate");
        }

        return extraButtons;
    }

    public String getLocationBuildingFromLookup() {
        return locationBuildingFromLookup;
    }

    public void setLocationBuildingFromLookup(String locationBuildingFromLookup) {
        this.locationBuildingFromLookup = locationBuildingFromLookup;
    }

    public String getLocationCampusFromLookup() {
        return locationCampusFromLookup;
    }

    public void setLocationCampusFromLookup(String locationCampusFromLookup) {
        this.locationCampusFromLookup = locationCampusFromLookup;
    }

    public String getInitialZipCode() {
        return initialZipCode;
    }

    public void setInitialZipCode(String initialZipCode) {
        this.initialZipCode = initialZipCode;
    }

    public boolean isCalculated() {
        return calculated;
    }

    public void setCalculated(boolean calculated) {
        this.calculated = calculated;
    }

    public boolean canUserCalculate(){
        return documentActions != null && documentActions.containsKey(KRADConstants.KUALI_ACTION_CAN_EDIT);
    }


    /**
     * @see org.kuali.kfs.sys.web.struts.KualiAccountingDocumentFormBase#repopulateOverrides(org.kuali.kfs.sys.businessobject.AccountingLine, java.lang.String, java.util.Map)
     */
    @Override
    protected void repopulateOverrides(AccountingLine line, String accountingLinePropertyName, Map parameterMap) {
        // do nothing; purchasing documents do not have overrides
    }

    /**
     * @return the URL to the line item import instructions
     */
    public String getLineItemImportInstructionsUrl() {
        return SpringContext.getBean(ConfigurationService.class).getPropertyValueAsString(KFSConstants.EXTERNALIZABLE_HELP_URL_KEY) + SpringContext.getBean(ParameterService.class).getParameterValueAsString(KfsParameterConstants.PURCHASING_DOCUMENT.class, PurapParameterConstants.LINE_ITEM_IMPORT);
    }
    
    @Override
    public boolean shouldMethodToCallParameterBeUsed(String methodToCallParameterName, String methodToCallParameterValue, HttpServletRequest request) {
    	// KFSPTS-985
        if (methodToCallParameterName.contains("addFavoriteAccount")) {
            return true;
        }
        return super.shouldMethodToCallParameterBeUsed(methodToCallParameterName, methodToCallParameterValue, request);
    }

	@Override
	public boolean shouldPropertyBePopulatedInForm(String requestParameterName,
			HttpServletRequest request) {
		// KFSPTS-985 : force it to populate
		if (requestParameterName.contains(".favoriteAccountLineIdentifier")) {
			return true;
		}
		return super.shouldPropertyBePopulatedInForm(requestParameterName, request);
	}
	
    public String getReasonToChange() {
        return reasonToChange;
    }

    public void setReasonToChange(String reasonToChange) {
        this.reasonToChange = reasonToChange;
    }
    
    public boolean isDocFinal() {
    	WorkflowDocument workflowDocument = this.getDocument().getDocumentHeader().getWorkflowDocument();
    	return workflowDocument != null ? workflowDocument.isApproved() : false;
    }
    public boolean isDocEnroute() {
    	WorkflowDocument workflowDocument = this.getDocument().getDocumentHeader().getWorkflowDocument();
    	return workflowDocument != null ? workflowDocument.isEnroute()  : false;
    }
    public boolean isDocCanceledOrDisapproved() {
    	WorkflowDocument workflowDocument = this.getDocument().getDocumentHeader().getWorkflowDocument();
    	return workflowDocument != null ? workflowDocument.isCanceled() ||  workflowDocument.isDisapproved() : false;
    }


}
