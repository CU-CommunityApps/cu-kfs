/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2023 Kuali, Inc.
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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.NormalizedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.core.api.impex.xml.XmlConstants;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.kew.xml.BooleanJaxbAdapter;
import org.kuali.kfs.kew.xml.SqlDateJaxbAdapter;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.KeyValuesService;
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
 * <p>Java class for anonymous complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://www.kuali.org/kfs/pdp/payment}payee_name"/&gt;
 *         &lt;element ref="{http://www.kuali.org/kfs/pdp/payment}payee_id" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.kuali.org/kfs/pdp/payment}payee_own_cd" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.kuali.org/kfs/pdp/payment}address1"/&gt;
 *         &lt;element ref="{http://www.kuali.org/kfs/pdp/payment}address2" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.kuali.org/kfs/pdp/payment}address3" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.kuali.org/kfs/pdp/payment}address4" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.kuali.org/kfs/pdp/payment}city" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.kuali.org/kfs/pdp/payment}state" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.kuali.org/kfs/pdp/payment}zip" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.kuali.org/kfs/pdp/payment}country" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.kuali.org/kfs/pdp/payment}campus_address_ind" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.kuali.org/kfs/pdp/payment}payment_date" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.kuali.org/kfs/pdp/payment}attachment_ind" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.kuali.org/kfs/pdp/payment}immediate_ind" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.kuali.org/kfs/pdp/payment}special_handling_ind" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.kuali.org/kfs/pdp/payment}taxable_ind" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.kuali.org/kfs/pdp/payment}nonresident_ind" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.kuali.org/kfs/pdp/payment}combine_group_ind" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.kuali.org/kfs/pdp/payment}bank_code" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.kuali.org/kfs/pdp/payment}detail" maxOccurs="27"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"payeeName",
    "payeeIdObj",
    "payeeOwnerCd",
    // CU customization
    "customerInstitutionNumber",
    "line1Address",
    "line2Address",
    "line3Address",
    "line4Address",
    "city",
    "state",
    "zipCd",
    "country",
    "campusAddress",
    "paymentDate",
    "pymtAttachment",
    "processImmediate",
    "pymtSpecialHandling",
    "taxablePayment",
    "nonresidentPayment",
    "combineGroups",
    "bankCode",
    "paymentDetails"})
public class PaymentGroup extends PersistableBusinessObjectBase {
	private static final long serialVersionUID = 1L;

    @XmlTransient
    private KualiInteger id;

    @XmlElement(name = "payee_name", namespace = XmlConstants.PAYMENT_NAMESPACE, required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    private String payeeName;

    @XmlElement(name = "payee_id", namespace = XmlConstants.PAYMENT_NAMESPACE)
    private PayeeId payeeIdObj;

    @XmlTransient
    private String payeeId;

    @XmlTransient
    private String payeeIdTypeCd;

    @XmlTransient
    private String alternatePayeeId;
    @XmlTransient
    private String alternatePayeeIdTypeCd;

    @XmlElement(name = "payee_own_cd", namespace = XmlConstants.PAYMENT_NAMESPACE)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    private String payeeOwnerCd;
    
    // CU customization
    @XmlElement(name = "customer_institution_identifier", namespace = XmlConstants.PAYMENT_NAMESPACE)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String customerInstitutionNumber;

    @XmlElement(namespace = XmlConstants.PAYMENT_NAMESPACE, name = "address1")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    private String line1Address;

    @XmlElement(namespace = XmlConstants.PAYMENT_NAMESPACE, name = "address2")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    private String line2Address;

    @XmlElement(namespace = XmlConstants.PAYMENT_NAMESPACE, name = "address3")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    private String line3Address;

    @XmlElement(namespace = XmlConstants.PAYMENT_NAMESPACE, name = "address4")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    private String line4Address;

    @XmlElement(namespace = XmlConstants.PAYMENT_NAMESPACE)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    private String city;

    @XmlElement(namespace = XmlConstants.PAYMENT_NAMESPACE)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    private String state;

    @XmlElement(namespace = XmlConstants.PAYMENT_NAMESPACE)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    private String country;

    @XmlElement(namespace = XmlConstants.PAYMENT_NAMESPACE, name = "zip")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    private String zipCd;

    @XmlElement(name = "campus_address_ind", namespace = XmlConstants.PAYMENT_NAMESPACE)
    @XmlJavaTypeAdapter(BooleanJaxbAdapter.class)
    private Boolean campusAddress = false;

    @XmlElement(name = "payment_date", namespace = XmlConstants.PAYMENT_NAMESPACE)
    @XmlJavaTypeAdapter(SqlDateJaxbAdapter.class)
    private Date paymentDate;

    @XmlElement(name = "attachment_ind", namespace = XmlConstants.PAYMENT_NAMESPACE)
    @XmlJavaTypeAdapter(BooleanJaxbAdapter.class)
    private Boolean pymtAttachment = false;

    @XmlElement(name = "special_handling_ind", namespace = XmlConstants.PAYMENT_NAMESPACE)
    @XmlJavaTypeAdapter(BooleanJaxbAdapter.class)
    private Boolean pymtSpecialHandling = false;

    @XmlElement(name = "taxable_ind", namespace = XmlConstants.PAYMENT_NAMESPACE)
    @XmlJavaTypeAdapter(BooleanJaxbAdapter.class)
    private Boolean taxablePayment = false;

    @XmlElement(name = "nonresident_ind", namespace = XmlConstants.PAYMENT_NAMESPACE)
    @XmlJavaTypeAdapter(BooleanJaxbAdapter.class)
    private Boolean nonresidentPayment = false;

    @XmlElement(name = "immediate_ind", namespace = XmlConstants.PAYMENT_NAMESPACE)
    @XmlJavaTypeAdapter(BooleanJaxbAdapter.class)
    private Boolean processImmediate = false;

    @XmlElement(name = "combine_group_ind", namespace = XmlConstants.PAYMENT_NAMESPACE)
    @XmlJavaTypeAdapter(BooleanJaxbAdapter.class)
    private Boolean combineGroups = false;

    @XmlTransient
    private String achBankRoutingNbr;
    @XmlTransient
    private String adviceEmailAddress;
    @XmlTransient
    private Boolean employeeIndicator;
    @XmlTransient
    private String creditMemoNbr;
    @XmlTransient
    private KualiDecimal creditMemoAmount;
    @XmlTransient
    private KualiInteger disbursementNbr;
    @XmlTransient
    private Date disbursementDate;
    @XmlTransient
    private Date originalDisbursementDate;
    @XmlTransient
    private String physCampusProcessCd;
    @XmlTransient
    private String sortValue;
    @XmlTransient
    private String achAccountType;
    @XmlTransient
    private Timestamp epicPaymentCancelledExtractedDate;
    @XmlTransient
    private Timestamp epicPaymentPaidExtractedDate;
    @XmlTransient
    private Timestamp adviceEmailSentDate;

    @XmlTransient
    private KualiInteger batchId;
    @XmlTransient
    private Batch batch;

    @XmlTransient
    private KualiInteger processId;
    @XmlTransient
    private PaymentProcess process;

    @XmlTransient
    private String paymentStatusCode;
    @XmlTransient
    private PaymentStatus paymentStatus;

    @XmlTransient
    private String disbursementTypeCode;
    @XmlTransient
    private DisbursementType disbursementType;

    @XmlElement(name = "bank_code", namespace = XmlConstants.PAYMENT_NAMESPACE)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    private String bankCode;

    @XmlTransient
    private Bank bank;

    @XmlTransient
    private AchAccountNumber achAccountNumber;

    @XmlTransient
    private List<PaymentGroupHistory> paymentGroupHistory = new ArrayList<>();

    @XmlElement(namespace = XmlConstants.PAYMENT_NAMESPACE, required = true, name = "detail")
    private List<PaymentDetail> paymentDetails = new ArrayList<>();

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
            refreshReferenceObject(PdpPropertyConstants.PAYMENT_STATUS);
        }

        // check for canceled and reissued
        String paymentStatusWithHistory = "";
        if (paymentStatus != null) {
            paymentStatusWithHistory += paymentStatus.getName();
        }

        boolean isCanceledReissued = false;
        for (final PaymentGroupHistory paymentGroupHistory : paymentGroupHistory) {
            if (PdpConstants.PaymentChangeCodes.CANCEL_REISSUE_DISBURSEMENT.equals(
                    paymentGroupHistory.getPaymentChangeCode())) {
                isCanceledReissued = true;
            }
        }

        if (isCanceledReissued) {
            paymentStatusWithHistory += " (Reissued)";
        }

        // CU Customization: KFSPTS-27383 do not add Stale to ACH or WIRE
        if (!isDisbursementTypeAchOrWire() && !StringUtils.equalsIgnoreCase(paymentStatus.getCode(), CRConstants.CLEARED)) {
            // check for stale payments, if one payment detail is stale then they all are
            final PaymentDetail paymentDetail = getPaymentDetails().get(0);
            if (!paymentDetail.isDisbursementActionAllowed()) {
                paymentStatusWithHistory += " (Stale)";
            }
        }

        return paymentStatusWithHistory;
    }

    private boolean isDisbursementTypeAchOrWire() {
        if (disbursementType == null) {
            return false;
        }
        return StringUtils.equals(disbursementType.getCode(), PdpConstants.DisbursementTypeCodes.ACH) ||
                StringUtils.equals(disbursementTypeCode, PdpConstants.DisbursementTypeCodes.WIRE);
    }

    /**
     * WIDTH MUST BE LESS THAN THE # OF SPACES
     *
     * @param width
     * @param val
     * @return
     */
    private String getWidthString(final int width, final String val) {
        return (val + "                                        ").substring(0, width - 1);
    }

    public int getNoteLines() {
        int count = 0;
        for (final PaymentDetail element : paymentDetails) {
            // Add a line for the invoice #
            count++;
            count += element.getNotes().size();
        }
        return count;
    }

    /**
     * @return the total of all the detail items.
     */
    public KualiDecimal getNetPaymentAmount() {
        KualiDecimal amt = KualiDecimal.ZERO;
        for (final PaymentDetail element : paymentDetails) {
            amt = amt.add(element.getNetPaymentAmount());
        }
        return amt;
    }

    public List<PaymentDetail> getPaymentDetails() {
        return paymentDetails;
    }

    /**
     * @param paymentDetails the payment details list to set.
     */
    public void setPaymentDetails(final List<PaymentDetail> paymentDetails) {
        this.paymentDetails = paymentDetails;
    }

    /**
     * This method adds a paymentDetail.
     *
     * @param pgh the payments detail to be added.
     */
    public void addPaymentDetails(final PaymentDetail pgh) {
        pgh.setPaymentGroup(this);
        paymentDetails.add(pgh);
    }

    public void deletePaymentDetails(final PaymentDetail pgh) {
        paymentDetails.remove(pgh);
    }

    public List<PaymentGroupHistory> getPaymentGroupHistory() {
        return paymentGroupHistory;
    }

    /**
     * @param paymentGroupHistory the payment group history list to set.
     */
    public void setPaymentGroupHistory(final List<PaymentGroupHistory> paymentGroupHistory) {
        this.paymentGroupHistory = paymentGroupHistory;
    }

    /**
     * This method adds a paymentGroupHistory.
     *
     * @param pd the paymentGroupHistory to be added.
     */
    public void addPaymentGroupHistory(final PaymentGroupHistory pd) {
        pd.setPaymentGroup(this);
        paymentGroupHistory.add(pd);
    }

    /**
     * This method deletes a paymentGroupHistory.
     *
     * @param pd the paymentGroupHistory to be deleted.
     */
    public void deletePaymentGroupHistory(final PaymentGroupHistory pd) {
        paymentGroupHistory.remove(pd);
    }

    public KualiInteger getId() {
        return id;
    }

    public AchAccountNumber getAchAccountNumber() {
        return achAccountNumber;
    }

    public void setAchAccountNumber(final AchAccountNumber achAccountNumber) {
        this.achAccountNumber = achAccountNumber;
    }

    public String getSortValue() {
        return sortValue;
    }

    public void setSortValue(final int sortGroupId) {
        final String defaultSortOrderParameterName = SpringContext.getBean(ConfigurationService.class)
                .getPropertyValueAsString(PdpKeyConstants.SORT_GROUP_ID);
        final String defaultSortOrderParameterValue = SpringContext.getBean(ParameterService.class)
                .getParameterValueAsString(PaymentGroup.class, defaultSortOrderParameterName);

        final StringBuilder sb = new StringBuilder();

        sb.append(sortGroupId);

        final CustomerProfile cp = batch.getCustomerProfile();
        sb.append(cp.getCampusCode());
        sb.append(getWidthString(4, cp.getUnitCode()));
        sb.append(getWidthString(4, cp.getSubUnitCode()));

        if (defaultSortOrderParameterValue.equals(String.valueOf(sortGroupId))) {
            sb.append(payeeId);
            sb.append(payeeIdTypeCd);
        } else {
            sb.append(payeeName);
        }
        sortValue = sb.toString();
    }

    public String getCity() {
        return city;
    }

    public void setCity(final String city) {
        this.city = city;
    }

    public Boolean getCombineGroups() {
        return combineGroups;
    }

    public void setCombineGroups(final Boolean combineGroups) {
        this.combineGroups = combineGroups;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(final String country) {
        this.country = country;
    }

    public String getState() {
        return state;
    }

    public void setState(final String state) {
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

    public void setBankCode(final String bankCode) {
        this.bankCode = bankCode;
    }

    // CU customization
    public String getCustomerInstitutionNumber() {
		return customerInstitutionNumber;
	}

    // CU customization
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

    public PayeeId getPayeeIdObj() {
        return payeeIdObj;
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

    public void setAchBankRoutingNbr(final String achBankRoutingNbr) {
        this.achBankRoutingNbr = achBankRoutingNbr;
    }

    public void setAdviceEmailAddress(final String adviceEmailAddress) {
        this.adviceEmailAddress = adviceEmailAddress;
    }

    public void setAlternatePayeeId(final String alternatePayeeId) {
        this.alternatePayeeId = alternatePayeeId;
    }

    public void setAlternatePayeeIdTypeCd(final String alternatePayeeIdTypeCd) {
        this.alternatePayeeIdTypeCd = alternatePayeeIdTypeCd;
    }

    public void setBank(final Bank bank) {
        this.bank = bank;
    }

    public void setBatch(final Batch batch) {
        this.batch = batch;
    }

    public void setCampusAddress(final Boolean campusAddress) {
        this.campusAddress = campusAddress;
    }

    public void setCreditMemoAmount(final KualiDecimal creditMemoAmount) {
        this.creditMemoAmount = creditMemoAmount;
    }

    public void setCreditMemoAmount(final String creditMemoAmount) {
        this.creditMemoAmount = new KualiDecimal(creditMemoAmount);
    }

    public void setCreditMemoNbr(final String creditMemoNbr) {
        this.creditMemoNbr = creditMemoNbr;
    }

    public void setDisbursementDate(final Date disbursementDate) {
        this.disbursementDate = disbursementDate;
    }

    /**
     * @throws ParseException
     */
    public void setDisbursementDate(final String disbursementDate) throws ParseException {
        this.disbursementDate = SpringContext.getBean(DateTimeService.class).convertToSqlDate(disbursementDate);
    }

    public void setDisbursementNbr(final KualiInteger disbursementNbr) {
        this.disbursementNbr = disbursementNbr;
    }

    public void setDisbursementNbr(final String disbursementNbr) {
        this.disbursementNbr = new KualiInteger(disbursementNbr);
    }

    public void setDisbursementType(final DisbursementType disbursementType) {
        this.disbursementType = disbursementType;
    }

    public void setId(final KualiInteger id) {
        this.id = id;
    }

    public void setEmployeeIndicator(final Boolean employeeIndicator) {
        this.employeeIndicator = employeeIndicator;
    }

    public void setLine1Address(final String line1Address) {
        this.line1Address = line1Address;
    }

    public void setLine2Address(final String line2Address) {
        this.line2Address = line2Address;
    }

    public void setLine3Address(final String line3Address) {
        this.line3Address = line3Address;
    }

    public void setLine4Address(final String line4Address) {
        this.line4Address = line4Address;
    }

    public void setNonresidentPayment(final Boolean nonresidentPayment) {
        this.nonresidentPayment = nonresidentPayment;
    }

    public void setPayeeId(final String payeeId) {
        this.payeeId = payeeId;
    }

    public void setPayeeIdTypeCd(final String payeeIdTypeCd) {
        this.payeeIdTypeCd = payeeIdTypeCd;
    }

    public void setPayeeName(final String payeeName) {
        this.payeeName = payeeName;
    }

    public void setPayeeOwnerCd(final String payeeOwnerCd) {
        this.payeeOwnerCd = payeeOwnerCd;
    }

    public void setPaymentDate(final Date paymentDate) {
        this.paymentDate = paymentDate;
    }

    /**
     * Takes a {@link String} and attempt to format as {@link Timestamp} for setting the paymentDate field.
     *
     * @param paymentDate Timestamp as string
     */
    public void setPaymentDate(final String paymentDate) throws ParseException {
        this.paymentDate = SpringContext.getBean(DateTimeService.class).convertToSqlDate(paymentDate);
    }

    public Date getOriginalDisbursementDate() {
        return originalDisbursementDate;
    }

    public void setOriginalDisbursementDate(final Date originalDisbursementDate) {
        this.originalDisbursementDate = originalDisbursementDate;
    }

    public void setPaymentStatus(final PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public void setPhysCampusProcessCd(final String physCampusProcessCd) {
        this.physCampusProcessCd = physCampusProcessCd;
    }

    public void setProcess(final PaymentProcess process) {
        if (process != null) {
            processId = process.getId();
        } else {
            processId = null;
        }
        this.process = process;
    }

    public void setProcessImmediate(final Boolean processImmediate) {
        this.processImmediate = processImmediate;
    }

    public void setPymtAttachment(final Boolean pymtAttachment) {
        this.pymtAttachment = pymtAttachment;
    }

    public void setTaxablePayment(final Boolean taxablePayment) {
        this.taxablePayment = taxablePayment;
    }

    public void setZipCd(final String zipCd) {
        this.zipCd = zipCd;
    }

    public void setPymtSpecialHandling(final Boolean pymtSpecialHandling) {
        this.pymtSpecialHandling = pymtSpecialHandling;
    }

    public String toStringKey() {
        final StringBuilder buffer = new StringBuilder();
        final CustomerProfile customerProfile = batch.getCustomerProfile();

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

    public void setAchAccountType(final String achAccountType) {
        this.achAccountType = achAccountType;
    }

    public Timestamp getEpicPaymentCancelledExtractedDate() {
        return epicPaymentCancelledExtractedDate;
    }

    public void setEpicPaymentCancelledExtractedDate(final Timestamp epicPaymentCancelledExtractedDate) {
        this.epicPaymentCancelledExtractedDate = epicPaymentCancelledExtractedDate;
    }

    public Timestamp getEpicPaymentPaidExtractedDate() {
        return epicPaymentPaidExtractedDate;
    }

    public void setEpicPaymentPaidExtractedDate(final Timestamp epicPaymentPaidExtractedDate) {
        this.epicPaymentPaidExtractedDate = epicPaymentPaidExtractedDate;
    }

    public KualiInteger getBatchId() {
        return batchId;
    }

    public void setBatchId(final KualiInteger batchId) {
        this.batchId = batchId;
    }

    public String getDisbursementTypeCode() {
        return disbursementTypeCode;
    }

    public void setDisbursementTypeCode(final String disbursementTypeCode) {
        this.disbursementTypeCode = disbursementTypeCode;
    }

    public KualiInteger getProcessId() {
        return processId;
    }

    public void setProcessId(final KualiInteger processId) {
        this.processId = processId;
    }

    public void setPaymentStatusCode(final String paymentStatusCode) {
        this.paymentStatusCode = paymentStatusCode;
    }

    public void setId_type(final String payeeIdTypeCd) {
        this.payeeIdTypeCd = payeeIdTypeCd;
    }

    public Timestamp getAdviceEmailSentDate() {
        return adviceEmailSentDate;
    }

    public void setAdviceEmailSentDate(final Timestamp adviceEmailSentDate) {
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
        final List<PayeeType> boList = (List) SpringContext.getBean(KeyValuesService.class).findAll(PayeeType.class);
        for (final PayeeType payeeType : boList) {
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
        setCountry(ObjectUtils.isNotNull(addr.getVendorCountry()) ? addr.getVendorCountry().getCode() : "");
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
    

    public void setPayeeOwnerCdFromVendor(String payeeIdValue) {

        BusinessObjectService bos = SpringContext.getBean(BusinessObjectService.class);
        String[] headerDetails = payeeIdValue.split("-");      
        Map<String, String> fieldValues = new HashMap<String, String>();
        fieldValues.put("vendorHeaderGeneratedIdentifier", headerDetails[0]/*payeeId*/);
        fieldValues.put("vendorDetailAssignedIdentifier", headerDetails[1]);
        
        List<VendorDetail> details = (List<VendorDetail>)bos.findMatching(VendorDetail.class, fieldValues);
        if (details.size() == 1) {
            payeeOwnerCd=details.get(0).getVendorHeader().getVendorOwnershipCode();
        } else {
            throw new RuntimeException("Could not locate Vendor Ownership Code for payeeId [ "+ payeeIdValue +" ]");
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
