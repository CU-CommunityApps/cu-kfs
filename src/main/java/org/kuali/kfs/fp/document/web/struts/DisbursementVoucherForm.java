/*
 * Copyright 2005 The Kuali Foundation
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl2.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.kfs.fp.document.web.struts;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.fp.businessobject.DisbursementVoucherNonEmployeeExpense;
import org.kuali.kfs.fp.businessobject.DisbursementVoucherPreConferenceRegistrant;
import org.kuali.kfs.fp.businessobject.TravelPerDiem;
import org.kuali.kfs.fp.document.DisbursementVoucherConstants;
import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.fp.document.service.DisbursementVoucherCoverSheetService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.UniversityDateService;
import org.kuali.kfs.sys.web.struts.KualiAccountingDocumentFormBase;
import org.kuali.rice.kns.service.KeyValuesService;
import org.kuali.rice.kns.service.KualiConfigurationService;
import org.kuali.rice.kns.service.ParameterService;
import org.kuali.rice.kns.util.KNSConstants;
import org.kuali.rice.kns.web.format.SimpleBooleanFormatter;

import edu.cornell.kfs.fp.document.service.CULegacyTravelService;
import edu.cornell.kfs.module.purap.document.service.IWantDocumentService;

/**
 * This class is the action form for the Disbursement Voucher.
 */
public class DisbursementVoucherForm extends KualiAccountingDocumentFormBase {
    protected static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(DisbursementVoucherForm.class);

    protected static final long serialVersionUID = 1L;

    protected String payeeIdNumber;
	protected String vendorHeaderGeneratedIdentifier = StringUtils.EMPTY;
    protected String vendorDetailAssignedIdentifier = StringUtils.EMPTY;
    protected String vendorAddressGeneratedIdentifier;
    
    protected String tempPayeeIdNumber;
    protected String tempVendorHeaderGeneratedIdentifier = StringUtils.EMPTY;
    protected String tempVendorDetailAssignedIdentifier = StringUtils.EMPTY;
    protected String tempVendorAddressGeneratedIdentifier;
    protected String oldPayeeType = StringUtils.EMPTY;
    
    protected boolean hasMultipleAddresses = false;

    protected DisbursementVoucherNonEmployeeExpense newNonEmployeeExpenseLine;
    protected DisbursementVoucherNonEmployeeExpense newPrePaidNonEmployeeExpenseLine;
    protected DisbursementVoucherPreConferenceRegistrant newPreConferenceRegistrantLine;
    protected String wireChargeMessage;
    
    protected boolean canExport = false;

    /**
     * Constructs a DisbursementVoucherForm.java.
     */
    public DisbursementVoucherForm() {
        super();
        setFormatterType("canPrintCoverSheet", SimpleBooleanFormatter.class);
    }

    @Override
    protected String getDefaultDocumentTypeName() {
        return "DV";
    }
    
    /**
     * @return Returns the newNonEmployeeExpenseLine.
     */
    public DisbursementVoucherNonEmployeeExpense getNewNonEmployeeExpenseLine() {
        return newNonEmployeeExpenseLine;
    }

    /**
     * @param newNonEmployeeExpenseLine The newNonEmployeeExpenseLine to set.
     */
    public void setNewNonEmployeeExpenseLine(DisbursementVoucherNonEmployeeExpense newNonEmployeeExpenseLine) {
        this.newNonEmployeeExpenseLine = newNonEmployeeExpenseLine;
    }

    /**
     * @return Returns the newPreConferenceRegistrantLine.
     */
    public DisbursementVoucherPreConferenceRegistrant getNewPreConferenceRegistrantLine() {
        return newPreConferenceRegistrantLine;
    }

    /**
     * @param newPreConferenceRegistrantLine The newPreConferenceRegistrantLine to set.
     */
    public void setNewPreConferenceRegistrantLine(DisbursementVoucherPreConferenceRegistrant newPreConferenceRegistrantLine) {
        this.newPreConferenceRegistrantLine = newPreConferenceRegistrantLine;
    }

    /**
     * @return Returns the newPrePaidNonEmployeeExpenseLine.
     */
    public DisbursementVoucherNonEmployeeExpense getNewPrePaidNonEmployeeExpenseLine() {
        return newPrePaidNonEmployeeExpenseLine;
    }

    /**
     * @param newPrePaidNonEmployeeExpenseLine The newPrePaidNonEmployeeExpenseLine to set.
     */
    public void setNewPrePaidNonEmployeeExpenseLine(DisbursementVoucherNonEmployeeExpense newPrePaidNonEmployeeExpenseLine) {
        this.newPrePaidNonEmployeeExpenseLine = newPrePaidNonEmployeeExpenseLine;
    }

    /**
     * @return Returns the wireChargeMessage.
     */
    public String getWireChargeMessage() {
        return wireChargeMessage;
    }

    /**
     * @param wireChargeMessage The wireChargeMessage to set.
     */
    public void setWireChargeMessage(String wireChargeMessage) {
        this.wireChargeMessage = wireChargeMessage;
    }

    /**
     * determines if the DV document is in a state that allows printing of the cover sheet
     * 
     * @return true if the DV document is in a state that allows printing of the cover sheet; otherwise, return false
     */
    public boolean getCanPrintCoverSheet() {
        DisbursementVoucherDocument disbursementVoucherDocument = (DisbursementVoucherDocument) this.getDocument();
        return SpringContext.getBean(DisbursementVoucherCoverSheetService.class).isCoverSheetPrintable(disbursementVoucherDocument);
    }

    /**
     * determines if the DV document is a travel DV and therefore should display the associated Trip #
     * 
     * @return true if the DV document is a travel DV; otherwise, return false
     */
    public boolean getCanViewTrip() {
    	DisbursementVoucherDocument disbursementVoucherDocument = (DisbursementVoucherDocument)this.getDocument();
    	boolean canViewTrip = SpringContext.getBean(CULegacyTravelService.class).isCULegacyTravelIntegrationInterfaceAssociatedWithTrip(disbursementVoucherDocument);
    	return canViewTrip;
    }

    /**
     * @return a list of available travel expense type codes for rendering per diem link page.
     */
    public List<TravelPerDiem> getTravelPerDiemCategoryCodes() {
        Map<String, Object> criteria = new HashMap<String, Object>();
        criteria.put(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, SpringContext.getBean(UniversityDateService.class).getCurrentFiscalYear());

        return (List<TravelPerDiem>) SpringContext.getBean(KeyValuesService.class).findMatching(TravelPerDiem.class, criteria);
    }

    /**
     * @return the per diem link message from the parameters table.
     */
    public String getTravelPerDiemLinkPageMessage() {
        return SpringContext.getBean(ParameterService.class).getParameterValue(DisbursementVoucherDocument.class, DisbursementVoucherConstants.TRAVEL_PER_DIEM_MESSAGE_PARM_NM);
    }

    /**
     * Gets the payeeIdNumber attribute.
     * 
     * @return Returns the payeeIdNumber.
     */
    public String getPayeeIdNumber() {
        return payeeIdNumber;
    }

    /**
     * Sets the payeeIdNumber attribute value.
     * 
     * @param payeeIdNumber The payeeIdNumber to set.
     */
    public void setPayeeIdNumber(String payeeIdNumber) {
        String separator = "-";
        if (this.isVendor() && StringUtils.contains(payeeIdNumber, separator)) {
            this.vendorHeaderGeneratedIdentifier = StringUtils.substringBefore(payeeIdNumber, separator);
            this.vendorDetailAssignedIdentifier = StringUtils.substringAfter(payeeIdNumber, separator);
        }

        this.payeeIdNumber = payeeIdNumber;
    }
    
	/**
     * Gets the payeeIdNumber attribute.
     * 
     * @return Returns the payeeIdNumber.
     */
    public String getTempPayeeIdNumber() {
        return tempPayeeIdNumber;
    }

    /**
     * Sets the payeeIdNumber attribute value.
     * 
     * @param payeeIdNumber The payeeIdNumber to set.
     */
    public void setTempPayeeIdNumber(String payeeIdNumber) {
        String separator = "-";
        if (this.isVendor() && StringUtils.contains(payeeIdNumber, separator)) {
            this.tempVendorHeaderGeneratedIdentifier = StringUtils.substringBefore(payeeIdNumber, separator);
            this.tempVendorDetailAssignedIdentifier = StringUtils.substringAfter(payeeIdNumber, separator);
        }

        this.tempPayeeIdNumber = payeeIdNumber;
    }
    
    /**
     * Gets the hasMultipleAddresses attribute.
     * 
     * @return Returns the hasMultipleAddresses.
     */
    public boolean hasMultipleAddresses() {
        return hasMultipleAddresses;
    }

    /**
     * Gets the hasMultipleAddresses attribute.
     * 
     * @return Returns the hasMultipleAddresses.
     */
    public boolean getHasMultipleAddresses() {
        return hasMultipleAddresses;
    }

    /**
     * Sets the hasMultipleAddresses attribute value.
     * 
     * @param hasMultipleAddresses The hasMultipleAddresses to set.
     */
    public void setHasMultipleAddresses(boolean hasMultipleAddresses) {
        this.hasMultipleAddresses = hasMultipleAddresses;
    }

    /**
     * Gets the vendorHeaderGeneratedIdentifier attribute.
     * 
     * @return Returns the vendorHeaderGeneratedIdentifier.
     */
    public String getVendorHeaderGeneratedIdentifier() {
        return vendorHeaderGeneratedIdentifier;
    }

    /**
     * Sets the vendorHeaderGeneratedIdentifier attribute value.
     * 
     * @param vendorHeaderGeneratedIdentifier The vendorHeaderGeneratedIdentifier to set.
     */
    public void setVendorHeaderGeneratedIdentifier(String vendorHeaderGeneratedIdentifier) {
        this.vendorHeaderGeneratedIdentifier = vendorHeaderGeneratedIdentifier;
    }

    /**
     * Gets the vendorDetailAssignedIdentifier attribute.
     * 
     * @return Returns the vendorDetailAssignedIdentifier.
     */
    public String getVendorDetailAssignedIdentifier() {
        return vendorDetailAssignedIdentifier;
    }

    /**
     * Sets the vendorDetailAssignedIdentifier attribute value.
     * 
     * @param vendorDetailAssignedIdentifier The vendorDetailAssignedIdentifier to set.
     */
    public void setVendorDetailAssignedIdentifier(String vendorDetailAssignedIdentifier) {
        this.vendorDetailAssignedIdentifier = vendorDetailAssignedIdentifier;
    }

    /**
     * Gets the vendorAddressGeneratedIdentifier attribute.
     * 
     * @return Returns the vendorAddressGeneratedIdentifier.
     */
    public String getVendorAddressGeneratedIdentifier() {
        return vendorAddressGeneratedIdentifier;
    }

    /**
     * Sets the vendorAddressGeneratedIdentifier attribute value.
     * 
     * @param vendorAddressGeneratedIdentifier The vendorAddressGeneratedIdentifier to set.
     */
    public void setVendorAddressGeneratedIdentifier(String vendorAddressGeneratedIdentifier) {
        this.vendorAddressGeneratedIdentifier = vendorAddressGeneratedIdentifier;
    }
    
    

    /**
     * Gets the tempVendorHeaderGeneratedIdentifier attribute. 
     * @return Returns the tempVendorHeaderGeneratedIdentifier.
     */
    public String getTempVendorHeaderGeneratedIdentifier() {
        return tempVendorHeaderGeneratedIdentifier;
    }

    /**
     * Sets the tempVendorHeaderGeneratedIdentifier attribute value.
     * @param tempVendorHeaderGeneratedIdentifier The tempVendorHeaderGeneratedIdentifier to set.
     */
    public void setTempVendorHeaderGeneratedIdentifier(String tempVendorHeaderGeneratedIdentifier) {
        this.tempVendorHeaderGeneratedIdentifier = tempVendorHeaderGeneratedIdentifier;
    }

    /**
     * Gets the tempVendorDetailAssignedIdentifier attribute. 
     * @return Returns the tempVendorDetailAssignedIdentifier.
     */
    public String getTempVendorDetailAssignedIdentifier() {
        return tempVendorDetailAssignedIdentifier;
    }

    /**
     * Sets the tempVendorDetailAssignedIdentifier attribute value.
     * @param tempVendorDetailAssignedIdentifier The tempVendorDetailAssignedIdentifier to set.
     */
    public void setTempVendorDetailAssignedIdentifier(String tempVendorDetailAssignedIdentifier) {
        this.tempVendorDetailAssignedIdentifier = tempVendorDetailAssignedIdentifier;
    }

    /**
     * Gets the tempVendorAddressGeneratedIdentifier attribute. 
     * @return Returns the tempVendorAddressGeneratedIdentifier.
     */
    public String getTempVendorAddressGeneratedIdentifier() {
        return tempVendorAddressGeneratedIdentifier;
    }

    /**
     * Sets the tempVendorAddressGeneratedIdentifier attribute value.
     * @param tempVendorAddressGeneratedIdentifier The tempVendorAddressGeneratedIdentifier to set.
     */
    public void setTempVendorAddressGeneratedIdentifier(String tempVendorAddressGeneratedIdentifier) {
        this.tempVendorAddressGeneratedIdentifier = tempVendorAddressGeneratedIdentifier;
    }
    
    

    /**
     * Gets the oldPayeeType attribute. 
     * @return Returns the oldPayeeType.
     */
    public String getOldPayeeType() {
        return oldPayeeType;
    }

    /**
     * Sets the oldPayeeType attribute value.
     * @param oldPayeeType The oldPayeeType to set.
     */
    public void setOldPayeeType(String oldPayeeType) {
        this.oldPayeeType = oldPayeeType;
    }

    /**
     * determine whether the selected payee is an employee
     */
    public boolean isEmployee() {
        DisbursementVoucherDocument disbursementVoucherDocument = (DisbursementVoucherDocument) this.getDocument();
        return disbursementVoucherDocument.getDvPayeeDetail().isEmployee();
    }

    /**
     * determine whether the selected payee is a student
     */
    public boolean isStudent() {
        DisbursementVoucherDocument disbursementVoucherDocument = (DisbursementVoucherDocument) this.getDocument();
        return disbursementVoucherDocument.getDvPayeeDetail().isStudent();
    }

    /**
     * determine whether the selected payee is an alumni
     */
    public boolean isAlumni() {
        DisbursementVoucherDocument disbursementVoucherDocument = (DisbursementVoucherDocument) this.getDocument();
        return disbursementVoucherDocument.getDvPayeeDetail().isAlumni();
    }

    /**
     * determine whether the selected payee is a vendor
     */
    public boolean isVendor() {
        DisbursementVoucherDocument disbursementVoucherDocument = (DisbursementVoucherDocument) this.getDocument();
        return disbursementVoucherDocument.getDvPayeeDetail().isVendor();
    }

    /**
     * @see org.kuali.rice.kns.web.struts.form.KualiDocumentFormBase#shouldMethodToCallParameterBeUsed(java.lang.String,
     *      java.lang.String, javax.servlet.http.HttpServletRequest)
     */
    @Override
    public boolean shouldMethodToCallParameterBeUsed(String methodToCallParameterName, String methodToCallParameterValue, HttpServletRequest request) {
        if (StringUtils.equals(methodToCallParameterName, KNSConstants.DISPATCH_REQUEST_PARAMETER)) {
            if (this.getExcludedmethodToCall().contains(methodToCallParameterValue)) {
                return true;
            }
        }
        return super.shouldMethodToCallParameterBeUsed(methodToCallParameterName, methodToCallParameterValue, request);
    }

    /**
     * @see org.kuali.kfs.sys.web.struts.KualiAccountingDocumentFormBase#getExcludedmethodToCall()
     */
    protected List<String> getExcludedmethodToCall() {
        List<String> execludedMethodToCall = super.getExcludedmethodToCall();
        execludedMethodToCall.add("printDisbursementVoucherCoverSheet");
        execludedMethodToCall.add("showTravelPerDiemLinks");

        return execludedMethodToCall;
    }
    
    /**
     * Gets the canExport attribute. 
     * @return Returns the canExport.
     */
    public boolean isCanExport() {
        return canExport;
    }

    /**
     * Sets the canExport attribute value.
     * @param canExport The canExport to set.
     */
    public void setCanExport(boolean canExport) {
        this.canExport = canExport;
    }
    
    /**
     * 
     * @param tripID
     * @return
     */
    public String getTripUrl() {
    	String tripID = this.getTripID();
    	StringBuffer url = new StringBuffer();
    	url.append(SpringContext.getBean(CULegacyTravelService.class).getTravelUrl());
        url.append("/navigation?form_action=0&tripid=").append(tripID).append("&link=true");
    	return url.toString();
    }
    
    /**
     * 
     * @return
     */
    public String getTripID() {
    	DisbursementVoucherDocument dvd = (DisbursementVoucherDocument) this.getDocument();
    	boolean isAssociated = SpringContext.getBean(CULegacyTravelService.class).isCULegacyTravelIntegrationInterfaceAssociatedWithTrip(dvd);
    	if (isAssociated) {
    		return dvd.getTripId();
    	} else {
    		return StringUtils.EMPTY;
    	}
    }
    
    // KFSPTS-2527
    /**
     * Determines if the DV document is a DV created from and I Want doc and therefore should display the associated I Wand Doc #
     * 
     * @return true if the DV document is a DV created from and I Want doc; otherwise, return false
     */
    public boolean getCanViewIWantDoc() {
        return SpringContext.getBean(IWantDocumentService.class).isDVgeneratedByIWantDoc(this.getDocId());
    }
    
    /**
     * Gets the IwantDocUrl for the related I Want Document if DV was created from an I Want doc.
     * @param IwantDocUrl
     * @return IwantDocUrl
     */
    public String getIwantDocUrl() {
        String tripID = getIwantDocID();
        LOG.info("getIWantDocUrl() called");
        StringBuffer url = new StringBuffer();
        url.append(SpringContext.getBean(KualiConfigurationService.class).getPropertyString(KFSConstants.WORKFLOW_URL_KEY) + "/DocHandler.do?docId=").append(tripID).append("&command=displayDocSearchView");
        return url.toString();
    }
    
    /**
     * Gets the IwantDocID for the related I Want Document if DV was created from an I Want doc.
     * @return IwantDocID
     */
    public String getIwantDocID() {
        return SpringContext.getBean(IWantDocumentService.class).getIWantDocIDByDVId(this.getDocId());
    }
    // end KFSPTS-2527
}
