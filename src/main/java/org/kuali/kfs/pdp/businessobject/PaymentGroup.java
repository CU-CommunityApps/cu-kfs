/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2022 Kuali, Inc.
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
package org.kuali.kfs.pdp.businessobject;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.KeyValuesService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.pdp.PdpKeyConstants;
import org.kuali.kfs.pdp.PdpPropertyConstants;
import org.kuali.kfs.pdp.service.AchService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.Bank;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.core.api.util.type.KualiInteger;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rsmart.kuali.kfs.cr.CRConstants;

/**
 * This class represents the PaymentGroup.
 */
public class PaymentGroup extends PersistableBusinessObjectBase {
	private static final long serialVersionUID = 1L;

	protected static KualiDecimal zero = KualiDecimal.ZERO;

    protected KualiInteger id;
    protected String payeeName;
    protected String payeeId;
    protected String payeeIdTypeCd;
    protected String alternatePayeeId;
    protected String alternatePayeeIdTypeCd;
    protected String payeeOwnerCd;
    protected String customerInstitutionNumber;
    protected String line1Address;
    protected String line2Address;
    protected String line3Address;
    protected String line4Address;
    protected String city;
    protected String state;
    protected String country;
    protected String zipCd;
    protected Boolean campusAddress;
    protected Date paymentDate;
    protected Boolean pymtAttachment;
    protected Boolean pymtSpecialHandling;
    protected Boolean taxablePayment;
    protected Boolean nonresidentPayment;
    protected Boolean processImmediate;
    protected Boolean combineGroups;
    protected String achBankRoutingNbr;
    protected String adviceEmailAddress;
    protected Boolean employeeIndicator;
    protected String creditMemoNbr;
    protected KualiDecimal creditMemoAmount;
    protected KualiInteger disbursementNbr;
    protected Date disbursementDate;
    protected String physCampusProcessCd;
    protected String sortValue;
    protected String achAccountType;
    protected Timestamp epicPaymentCancelledExtractedDate;
    protected Timestamp epicPaymentPaidExtractedDate;
    protected Timestamp adviceEmailSentDate;

    protected KualiInteger batchId;
    protected Batch batch;

    protected KualiInteger processId;
    protected PaymentProcess process;

    protected String paymentStatusCode;
    protected PaymentStatus paymentStatus;

    protected String disbursementTypeCode;
    protected DisbursementType disbursementType;

    protected String bankCode;
    protected Bank bank;

    protected AchAccountNumber achAccountNumber;

    protected List<PaymentGroupHistory> paymentGroupHistory = new ArrayList<>();
    protected List<PaymentDetail> paymentDetails = new ArrayList<>();

    public PaymentGroup() {
        super();
    }

    public boolean isDailyReportSpecialHandling() {
        return pymtSpecialHandling && !processImmediate;
    }

    public boolean isDailyReportAttachment() {
        return !pymtSpecialHandling && !processImmediate && pymtAttachment;
    }

    public String getPaymentStatusCode() {
        return paymentStatusCode;
    }

    /**
     * @return String containing the payment status code and indication or cancel/reissued payments or stale payments.
     */
    public String getPaymentStatusCodeWithHistory() {
        if (paymentStatus == null) {
            this.refreshReferenceObject(PdpPropertyConstants.PAYMENT_STATUS);
        }

        // check for canceled and reissued
        String paymentStatusWithHistory = "";
        if (paymentStatus != null) {
            paymentStatusWithHistory += paymentStatus.getName();
        }

        boolean isCanceledReissued = false;
        for (PaymentGroupHistory paymentGroupHistory : getPaymentGroupHistory()) {
            if (PdpConstants.PaymentChangeCodes.CANCEL_REISSUE_DISBURSEMENT.equals(
                    paymentGroupHistory.getPaymentChangeCode())) {
                isCanceledReissued = true;
            }
        }

        if (isCanceledReissued) {
            paymentStatusWithHistory += " (Reissued)";
        }
        
        // check for stale payments, if one payment detail is stale then they all are

        if (!CRConstants.CLEARED.equalsIgnoreCase(paymentStatus.getCode())) {
            PaymentDetail paymentDetail = getPaymentDetails().get(0);
            if (!paymentDetail.isDisbursementActionAllowed()) {
                paymentStatusWithHistory += " (Stale)";
            }
        }

        return paymentStatusWithHistory;
    }

    /**
     * WIDTH MUST BE LESS THAN THE # OF SPACES
     *
     * @param width
     * @param val
     * @return
     */
    protected String getWidthString(int width, String val) {
        return (val + "                                        ").substring(0, width - 1);
    }

    public int getNoteLines() {
        int count = 0;
        for (PaymentDetail element : this.getPaymentDetails()) {
            // Add a line for the invoice #
            count++;
            count = count + element.getNotes().size();
        }
        return count;
    }

    /**
     * @return the total of all the detail items.
     */
    public KualiDecimal getNetPaymentAmount() {
        KualiDecimal amt = KualiDecimal.ZERO;
        for (PaymentDetail element : this.getPaymentDetails()) {
            amt = amt.add(element.getNetPaymentAmount());
        }
        return amt;
    }

    public List<PaymentDetail> getPaymentDetails() {
        return paymentDetails;
    }

    /**
     * @param paymentDetail the payment details list to set.
     */
    public void setPaymentDetails(List<PaymentDetail> paymentDetail) {
        this.paymentDetails = paymentDetail;
    }

    /**
     * This method adds a paymentDetail.
     *
     * @param pgh the payments detail to be added.
     */
    public void addPaymentDetails(PaymentDetail pgh) {
        pgh.setPaymentGroup(this);
        paymentDetails.add(pgh);
    }

    public void deletePaymentDetails(PaymentDetail pgh) {
        paymentDetails.remove(pgh);
    }

    public List<PaymentGroupHistory> getPaymentGroupHistory() {
        return paymentGroupHistory;
    }

    /**
     * @param paymentGroupHistory the payment group history list to set.
     */
    public void setPaymentGroupHistory(List<PaymentGroupHistory> paymentGroupHistory) {
        this.paymentGroupHistory = paymentGroupHistory;
    }

    /**
     * This method adds a paymentGroupHistory.
     *
     * @param pd the paymentGroupHistory to be added.
     */
    public void addPaymentGroupHistory(PaymentGroupHistory pd) {
        pd.setPaymentGroup(this);
        paymentGroupHistory.add(pd);
    }

    /**
     * This method deletes a paymentGroupHistory.
     *
     * @param pd the paymentGroupHistory to be deleted.
     */
    public void deletePaymentGroupHistory(PaymentGroupHistory pd) {
        paymentGroupHistory.remove(pd);
    }

    public KualiInteger getId() {
        return id;
    }

    public AchAccountNumber getAchAccountNumber() {
        return achAccountNumber;
    }

    public void setAchAccountNumber(AchAccountNumber aan) {
        this.achAccountNumber = aan;
    }

    public String getSortValue() {
        return sortValue;
    }

    public void setSortValue(int sortGroupId) {
        String defaultSortOrderParameterName = SpringContext.getBean(ConfigurationService.class)
                .getPropertyValueAsString(PdpKeyConstants.DEFAULT_SORT_GROUP_ID);
        String defaultSortOrderParameterValue = SpringContext.getBean(ParameterService.class).getParameterValueAsString(
                PaymentGroup.class, defaultSortOrderParameterName);

        StringBuffer sb = new StringBuffer();

        sb.append(sortGroupId);

        CustomerProfile cp = this.getBatch().getCustomerProfile();
        sb.append(cp.getCampusCode());
        sb.append(getWidthString(4, cp.getUnitCode()));
        sb.append(getWidthString(4, cp.getSubUnitCode()));

        if (defaultSortOrderParameterValue.equals(String.valueOf(sortGroupId))) {
            sb.append(this.getPayeeId());
            sb.append(this.getPayeeIdTypeCd());
        } else {
            sb.append(this.getPayeeName());
        }
        this.sortValue = sb.toString();
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Boolean getCombineGroups() {
        return combineGroups;
    }

    public void setCombineGroups(Boolean combineGroups) {
        this.combineGroups = combineGroups;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getAchBankRoutingNbr() {
        return achBankRoutingNbr;
    }

    public String getAdviceEmailAddress() {
        return adviceEmailAddress;
    }

    public String getAlternatePayeeId() {
        return alternatePayeeId;
    }

    public String getAlternatePayeeIdTypeCd() {
        return alternatePayeeIdTypeCd;
    }

    public Bank getBank() {
        return bank;
    }

    public Batch getBatch() {
        if (ObjectUtils.isNull(batch) && ObjectUtils.isNotNull(batchId)) {
            this.refreshReferenceObject("batch");
        }
        return batch;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getCustomerInstitutionNumber() {
		return customerInstitutionNumber;
	}

	public void setCustomerInstitutionNumber(String customerInstitutionNumber) {
		this.customerInstitutionNumber = customerInstitutionNumber;
	}

    public Boolean getCampusAddress() {
        return campusAddress;
    }

    public KualiDecimal getCreditMemoAmount() {
        return creditMemoAmount;
    }

    public String getCreditMemoNbr() {
        return creditMemoNbr;
    }

    public Date getDisbursementDate() {
        return disbursementDate;
    }

    public KualiInteger getDisbursementNbr() {
        return disbursementNbr;
    }

    public DisbursementType getDisbursementType() {
        return disbursementType;
    }

    public Boolean getEmployeeIndicator() {
        return employeeIndicator;
    }

    public String getLine1Address() {
        return line1Address;
    }

    public String getLine2Address() {
        return line2Address;
    }

    public String getLine3Address() {
        return line3Address;
    }

    public String getLine4Address() {
        return line4Address;
    }

    public Boolean getNonresidentPayment() {
        return nonresidentPayment;
    }

    public String getPayeeId() {
        return payeeId;
    }

    public String getPayeeIdTypeCd() {
        return payeeIdTypeCd;
    }

    public String getPayeeName() {
        return payeeName;
    }

    public String getPayeeOwnerCd() {
        return payeeOwnerCd;
    }

    public Date getPaymentDate() {
        return paymentDate;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public String getPhysCampusProcessCd() {
        return physCampusProcessCd;
    }

    public PaymentProcess getProcess() {
        return process;
    }

    public Boolean getProcessImmediate() {
        return processImmediate;
    }

    public Boolean getPymtAttachment() {
        return pymtAttachment;
    }

    public Boolean getPymtSpecialHandling() {
        return pymtSpecialHandling;
    }

    public Boolean getTaxablePayment() {
        return taxablePayment;
    }

    public String getZipCd() {
        return zipCd;
    }

    public void setAchBankRoutingNbr(String s) {
        achBankRoutingNbr = s;
    }

    public void setAdviceEmailAddress(String string) {
        adviceEmailAddress = string;
    }

    public void setAlternatePayeeId(String string) {
        alternatePayeeId = string;
    }

    public void setAlternatePayeeIdTypeCd(String string) {
        alternatePayeeIdTypeCd = string;
    }

    public void setBank(Bank bank) {
        this.bank = bank;
    }

    public void setBatch(Batch b) {
        batch = b;
    }

    public void setCampusAddress(Boolean boolean1) {
        campusAddress = boolean1;
    }

    public void setCreditMemoAmount(KualiDecimal decimal) {
        creditMemoAmount = decimal;
    }

    public void setCreditMemoAmount(String decimal) {
        creditMemoAmount = new KualiDecimal(decimal);
    }

    public void setCreditMemoNbr(String string) {
        creditMemoNbr = string;
    }

    public void setDisbursementDate(Date timestamp) {
        disbursementDate = timestamp;
    }

    /**
     * @throws ParseException
     */
    public void setDisbursementDate(String disbursementDate) throws ParseException {
        this.disbursementDate = SpringContext.getBean(DateTimeService.class).convertToSqlDate(disbursementDate);
    }

    public void setDisbursementNbr(KualiInteger integer) {
        disbursementNbr = integer;
    }

    public void setDisbursementNbr(String integer) {
        disbursementNbr = new KualiInteger(integer);
    }

    public void setDisbursementType(DisbursementType dt) {
        disbursementType = dt;
    }

    public void setId(KualiInteger integer) {
        id = integer;
    }

    public void setEmployeeIndicator(Boolean boolean1) {
        employeeIndicator = boolean1;
    }

    public void setLine1Address(String string) {
        line1Address = string;
    }

    public void setLine2Address(String string) {
        line2Address = string;
    }

    public void setLine3Address(String string) {
        line3Address = string;
    }

    public void setLine4Address(String string) {
        line4Address = string;
    }

    public void setNonresidentPayment(Boolean boolean1) {
        nonresidentPayment = boolean1;
    }

    public void setPayeeId(String string) {
        payeeId = string;
    }

    public void setPayeeIdTypeCd(String string) {
        payeeIdTypeCd = string;
    }

    public void setPayeeName(String string) {
        payeeName = string;
    }

    public void setPayeeOwnerCd(String string) {
        payeeOwnerCd = string;
    }

    public void setPaymentDate(Date timestamp) {
        paymentDate = timestamp;
    }

    /**
     * Takes a {@link String} and attempt to format as {@link Timestamp} for setting the paymentDate field.
     *
     * @param paymentDate Timestamp as string
     */
    public void setPaymentDate(String paymentDate) throws ParseException {
        this.paymentDate = SpringContext.getBean(DateTimeService.class).convertToSqlDate(paymentDate);
    }

    public void setPaymentStatus(PaymentStatus stat) {
        paymentStatus = stat;
    }

    public void setPhysCampusProcessCd(String string) {
        physCampusProcessCd = string;
    }

    public void setProcess(PaymentProcess p) {
        if (p != null) {
            processId = p.getId();
        } else {
            processId = null;
        }
        this.process = p;
    }

    public void setProcessImmediate(Boolean boolean1) {
        processImmediate = boolean1;
    }

    public void setPymtAttachment(Boolean boolean1) {
        pymtAttachment = boolean1;
    }

    public void setTaxablePayment(Boolean boolean1) {
        taxablePayment = boolean1;
    }

    public void setZipCd(String string) {
        zipCd = string;
    }

    public void setPymtSpecialHandling(Boolean pymtSpecialHandling) {
        this.pymtSpecialHandling = pymtSpecialHandling;
    }

    public String toStringKey() {
        StringBuffer buffer = new StringBuffer();
        CustomerProfile customerProfile = batch.getCustomerProfile();

        buffer.append(PdpPropertyConstants.CustomerProfile.CUSTOMER_PROFILE_CAMPUS_CODE);
        buffer.append("=");
        buffer.append(customerProfile.getCampusCode());
        buffer.append(PdpPropertyConstants.CustomerProfile.CUSTOMER_PROFILE_UNIT_CODE);
        buffer.append("=");
        buffer.append(customerProfile.getUnitCode());
        buffer.append(PdpPropertyConstants.CustomerProfile.CUSTOMER_PROFILE_SUB_UNIT_CODE);
        buffer.append("=");
        buffer.append(customerProfile.getSubUnitCode());
        buffer.append(PdpPropertyConstants.PaymentGroup.PAYMENT_GROUP_PAYEE_NAME);
        buffer.append("=");
        buffer.append(payeeName);
        buffer.append(PdpPropertyConstants.PaymentGroup.PAYMENT_GROUP_LINE1_ADDRESS);
        buffer.append("=");
        buffer.append(line1Address);
        buffer.append(PdpPropertyConstants.PaymentGroup.PAYMENT_GROUP_PAYEE_ID);
        buffer.append("=");
        buffer.append(payeeId);
        buffer.append(PdpPropertyConstants.PaymentGroup.PAYMENT_GROUP_PAYEE_ID_TYPE_CODE);
        buffer.append("=");
        buffer.append(payeeIdTypeCd);
        buffer.append(PdpPropertyConstants.PaymentGroup.PAYMENT_GROUP_BANK_CODE);
        buffer.append("=");
        buffer.append(bankCode);

        return buffer.toString();
    }

    public String getAchAccountType() {
        return achAccountType;
    }

    public void setAchAccountType(String achAccountType) {
        this.achAccountType = achAccountType;
    }

    public Timestamp getEpicPaymentCancelledExtractedDate() {
        return epicPaymentCancelledExtractedDate;
    }

    public void setEpicPaymentCancelledExtractedDate(Timestamp epicPaymentCancelledExtractedDate) {
        this.epicPaymentCancelledExtractedDate = epicPaymentCancelledExtractedDate;
    }

    public Timestamp getEpicPaymentPaidExtractedDate() {
        return epicPaymentPaidExtractedDate;
    }

    public void setEpicPaymentPaidExtractedDate(Timestamp epicPaymentPaidExtractedDate) {
        this.epicPaymentPaidExtractedDate = epicPaymentPaidExtractedDate;
    }

    public KualiInteger getBatchId() {
        return batchId;
    }

    public void setBatchId(KualiInteger batchId) {
        this.batchId = batchId;
    }

    public String getDisbursementTypeCode() {
        return disbursementTypeCode;
    }

    public void setDisbursementTypeCode(String disbursementTypeCode) {
        this.disbursementTypeCode = disbursementTypeCode;
    }

    public KualiInteger getProcessId() {
        return processId;
    }

    public void setProcessId(KualiInteger processId) {
        this.processId = processId;
    }

    public void setPaymentStatusCode(String paymentStatusCode) {
        this.paymentStatusCode = paymentStatusCode;
    }

    public void setId_type(String idType) {
        this.payeeIdTypeCd = idType;
    }

    public Timestamp getAdviceEmailSentDate() {
        return adviceEmailSentDate;
    }

    public void setAdviceEmailSentDate(Timestamp adviceEmailSentDate) {
        this.adviceEmailSentDate = adviceEmailSentDate;
    }

    /**
     * @return the street as a combined representation of the address lines
     */
    public String getStreet() {
        return (StringUtils.isNotBlank(line1Address) ? line1Address + KFSConstants.NEWLINE : KFSConstants.EMPTY_STRING) +
                (StringUtils.isNotBlank(line2Address) ? line2Address + KFSConstants.NEWLINE : KFSConstants.EMPTY_STRING) +
                (StringUtils.isNotBlank(line3Address) ? line3Address + KFSConstants.NEWLINE : KFSConstants.EMPTY_STRING) +
                (StringUtils.isNotBlank(line4Address) ? line4Address + KFSConstants.NEWLINE : KFSConstants.EMPTY_STRING);
    }
    
    public void validateVendorIdAndCustomerInstitutionIdentifier() {
        
        BusinessObjectService bos = SpringContext.getBean(BusinessObjectService.class);
        Map<String, String> fieldValues = new HashMap<String, String>();
        fieldValues.put("vendorAddressGeneratedIdentifier", customerInstitutionNumber);
        String[] headerDetails = payeeId.split("-");
        fieldValues.put("vendorHeaderGeneratedIdentifier", headerDetails[0]/*payeeId*/);
        fieldValues.put("vendorDetailAssignedIdentifier", headerDetails[1]);
        
        List<VendorAddress> addrs = (List<VendorAddress>)bos.findMatching(VendorAddress.class, fieldValues);
        if (addrs.size() == 1) {
            VendorAddress addr = (VendorAddress) addrs.get(0);
            setVendorAddress(addr);
        } else {
            throw new RuntimeException("Invalid Address [ "+customerInstitutionNumber+" ] for payee [ "+payeeId + " ]");
            // Need to handle bad data.
        }
    }

    public String getPayeeIdTypeDesc() {
        String payeeIdTypeCd = getPayeeIdTypeCd();
        List<PayeeType> boList = (List) SpringContext.getBean(KeyValuesService.class).findAll(PayeeType.class);
        for (PayeeType payeeType : boList) {
            if (payeeType.getCode().equalsIgnoreCase(payeeIdTypeCd)) {
                return payeeType.getName();
            }
        }
        return KFSConstants.EMPTY_STRING;
    }
    /**
     * Setter that takes in a VendorAddress and parses up the values to assign to the individual attributes in the PaymentGroup.
     * 
     * @param addr
     */
    public void setVendorAddress(VendorAddress addr) {
        setLine1Address(addr.getVendorLine1Address());
        setLine2Address(addr.getVendorLine2Address());
        setCity(addr.getVendorCityName());
        setState(ObjectUtils.isNotNull(addr.getVendorState()) ? addr.getVendorState().getCode() : "");
        setZipCd(addr.getVendorZipCode());
        setCountry(ObjectUtils.isNotNull(addr.getVendorCountry()) ? addr.getVendorCountry().getName() : "");
    }
    
    /**
     * @param string
     */
    public void setPayeeIdAndName(String string) {
        if (string.split("-").length == 1) {
            // process Payee as a employee
            payeeId = string;
        } else {
            // process Payee as a vendor
            payeeId = string;

            BusinessObjectService bos = SpringContext.getBean(BusinessObjectService.class);
            String[] headerDetails = payeeId.split("-");
            Map<String, String> fieldValues = new HashMap<String, String>();
            fieldValues.put("vendorHeaderGeneratedIdentifier", headerDetails[0]/* payeeId */);
            fieldValues.put("vendorDetailAssignedIdentifier", headerDetails[1]);

            List<VendorDetail> details = (List<VendorDetail>) bos.findMatching(VendorDetail.class, fieldValues);
            if (details.size() == 1) {
                payeeName = details.get(0).getVendorName();
            } else {
                throw new RuntimeException("Could not locate Vendor for payeeId [ " + string + " ]");
            }
        }
    }
    
    /**
     * @param string
     */
    public void setPayeeOwnerCdFromVendor(String string) {
        
       // payeeOwnerCd = string;
        
        
        BusinessObjectService bos = SpringContext.getBean(BusinessObjectService.class);
        String[] headerDetails = payeeId.split("-");      
        Map<String, String> fieldValues = new HashMap<String, String>();
        fieldValues.put("vendorHeaderGeneratedIdentifier", headerDetails[0]/*payeeId*/);
        fieldValues.put("vendorDetailAssignedIdentifier", headerDetails[1]);
        
        List<VendorDetail> details = (List<VendorDetail>)bos.findMatching(VendorDetail.class, fieldValues);
        if (details.size() == 1) {
            payeeOwnerCd=details.get(0).getVendorHeader().getVendorOwnershipCode();
        } else {
            throw new RuntimeException("Could not locate Vendor Ownership Code for payeeId [ "+ string+" ]");
        }
    }
    
    /**
     * The logic used in the payableByACH() method was taken from another part of KFS and is based on the presence or absence of an ACH account for the defined payee.  
     * Because of this logic, it can be inferred that if a payment cannot be paid by ACH, then it must be payable by check.  
     * 
     * If additional payments methods are introduced, this logic will need to be re-written.
     * 
     * @return True if payable by check, false otherwise.
     */
    public boolean isPayableByCheck() {
        return !isPayableByACH();
    }
    
    /**
     * A payment group is payable by ACH if it meets all of the following criteria:
     * - the sum of payment details within a group total to a positive value (criteria added with KFSPTS-1460)
     * - there are no negative payment details within the group (criteria removed with KFSPTS-1460)
     * - the payee has a defined ACH account
     * - the payment does not contain any attachments, special handling and is not an immediate payment
     * 
     * @return True if the payment group is payable by ACH, false otherwise.
     */
    public boolean isPayableByACH() {
        
        //KFSPTS-1460:
        // Replaced the logic: 
        // If any one of the payment details in the group are negative, we always force a check
        //
        // With the new logic: 
        // If the total amount for all the payment details within a payment group is positive ALLOW ACH; 
        // otherwise force a check because the total is negative.       
        boolean paymentGroupTotalIsNotNegative = true;
        KualiDecimal paymentGroupTotal = KualiDecimal.ZERO;
        List<PaymentDetail> paymentDetailsList = getPaymentDetails();
        for (PaymentDetail paymentDetail : paymentDetailsList) {
            paymentGroupTotal = paymentGroupTotal.add(paymentDetail.getNetPaymentAmount());
        }
        if (paymentGroupTotal.isNegative()) { 
            paymentGroupTotalIsNotNegative = false;
        }
        

        // determine whether payment should be ACH or Check
        CustomerProfile customer = getBatch().getCustomerProfile();
        
        PayeeACHAccount payeeAchAccount = null;
        boolean isACH = false;
        if (PdpConstants.PayeeIdTypeCodes.VENDOR_ID.equals(getPayeeIdTypeCd()) || PdpConstants.PayeeIdTypeCodes.EMPLOYEE.equals(getPayeeIdTypeCd()) 
                || PdpConstants.PayeeIdTypeCodes.ENTITY.equals(getPayeeIdTypeCd())) {
            if (StringUtils.isNotBlank(getPayeeId()) && !getPymtAttachment() && !getProcessImmediate() 
                    && !getPymtSpecialHandling() && (customer.getAchTransactionType() != null) && paymentGroupTotalIsNotNegative) {
                payeeAchAccount = SpringContext.getBean(AchService.class).getAchInformation(getPayeeIdTypeCd(), getPayeeId(), customer.getAchTransactionType());
                isACH = ObjectUtils.isNotNull(payeeAchAccount);
            }
        }

        return isACH;
    }
    
    @Override
    public void beforeInsert() {
        super.beforeInsert();
        Timestamp lastUpdateTemp = this.getLastUpdatedTimestamp();

        if (ObjectUtils.isNull(lastUpdateTemp)) {
            this.setLastUpdatedTimestamp(new Timestamp(new java.util.Date().getTime()));
        } else {
            this.setLastUpdatedTimestamp(lastUpdateTemp);
        }
    }

    @Override
    public void beforeUpdate() {
        super.beforeUpdate();
        Timestamp lastUpdateTemp = this.getLastUpdatedTimestamp();

        if (ObjectUtils.isNull(lastUpdateTemp)) {
            this.setLastUpdatedTimestamp(new Timestamp(new java.util.Date().getTime()));
        } else {
            this.setLastUpdatedTimestamp(lastUpdateTemp);
        }
    }

}
