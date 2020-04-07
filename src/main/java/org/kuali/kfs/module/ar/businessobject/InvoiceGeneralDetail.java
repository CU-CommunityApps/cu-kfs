/**
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2019 Kuali, Inc.
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
package org.kuali.kfs.module.ar.businessobject;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.integration.ar.Billable;
import org.kuali.kfs.integration.cg.ContractsAndGrantsBillingAward;
import org.kuali.kfs.integration.cg.ContractsAndGrantsInstrumentType;
import org.kuali.kfs.integration.cg.ContractsAndGrantsModuleBillingService;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.krad.service.KualiModuleService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.ar.ArPropertyConstants;
import org.kuali.kfs.module.ar.document.ContractsGrantsInvoiceDocument;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

public class InvoiceGeneralDetail extends PersistableBusinessObjectBase implements Billable {

    private String documentNumber;
    private String comment;
    private String awardDateRange;
    private String agencyNumber;
    private String billingFrequencyCode;
    private boolean finalBillIndicator;
    private String billingPeriod;
    private String instrumentTypeCode;
    private KualiDecimal awardTotal = KualiDecimal.ZERO;
    private KualiDecimal totalAmountBilledToDate = KualiDecimal.ZERO;
    private KualiDecimal totalPreviouslyBilled = KualiDecimal.ZERO;
    private KualiDecimal costShareAmount = KualiDecimal.ZERO;
    private Date lastBilledDate;
    private String dunningLetterTemplateAssigned;
    private Date dunningLetterTemplateSentDate;
    private String proposalNumber;
    private String awardNumber;
    private Integer sequenceNumber;
    // To categorize the CG Invoices based on Award LOC Type
    private String letterOfCreditCreationType;
    private String letterOfCreditFundGroupCode;
    private String letterOfCreditFundCode;

    private ContractsGrantsInvoiceDocument invoiceDocument;
    private ContractsAndGrantsBillingAward award;
    
    /*
     *  Brought in InvoiceGeneralDetail as a new overlay class to back port FINP-6611 from base code as the version of 
     *  KFS we overlay does not have kualiModuleService as a transient service. FINP-6611 makes it transient.
     *  This overlay should be removed when the 3/26 version of KFS is brought in
     */
    private transient KualiModuleService kualiModuleService;
    private transient ContractsAndGrantsModuleBillingService contractsAndGrantsModuleBillingService;

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getAgencyNumber() {
        return agencyNumber;
    }

    public void setAgencyNumber(String agencyNumber) {
        this.agencyNumber = agencyNumber;
    }

    public String getAwardDateRange() {
        return awardDateRange;
    }

    public void setAwardDateRange(String awardDateRange) {
        this.awardDateRange = awardDateRange;
    }

    public String getBillingFrequencyCode() {
        return billingFrequencyCode;
    }

    public void setBillingFrequencyCode(String billingFrequencyCode) {
        this.billingFrequencyCode = billingFrequencyCode;
    }

    public boolean isFinalBillIndicator() {
        return finalBillIndicator;
    }

    public void setFinalBillIndicator(boolean finalBillIndicator) {
        this.finalBillIndicator = finalBillIndicator;
    }

    public String getBillingPeriod() {
        return billingPeriod;
    }

    public void setBillingPeriod(String billingPeriod) {
        this.billingPeriod = billingPeriod;
    }

    public String getInstrumentTypeCode() {
        return instrumentTypeCode;
    }

    public void setInstrumentTypeCode(String instrumentTypeCode) {
        this.instrumentTypeCode = instrumentTypeCode;
    }

    public String getInstrumentTypeDescription() {
        if (StringUtils.isNotBlank(instrumentTypeCode)) {
            Map<String, Object> map = new HashMap<>();
            map.put(ArPropertyConstants.INSTRUMENT_TYPE_CODE, instrumentTypeCode);

            ContractsAndGrantsInstrumentType instrumentType = getKualiModuleService()
                    .getResponsibleModuleService(ContractsAndGrantsInstrumentType.class)
                    .getExternalizableBusinessObject(ContractsAndGrantsInstrumentType.class, map);

            if (instrumentType != null) {
                return instrumentType.getInstrumentTypeDescription();
            }
        }

        return null;
    }

    public KualiDecimal getAwardTotal() {
        return awardTotal;
    }

    public void setAwardTotal(KualiDecimal awardTotal) {
        this.awardTotal = awardTotal;
    }

    public KualiDecimal getTotalAmountBilledToDate() {
        return totalAmountBilledToDate;
    }

    public void setTotalAmountBilledToDate(KualiDecimal totalAmountBilledToDate) {
        this.totalAmountBilledToDate = totalAmountBilledToDate;
    }

    /**
     * Gets the amountRemainingToBill attribute.
     *
     * @return the calculated amountRemainingToBill (the award total minus the amount billed to date)
     */
    public KualiDecimal getAmountRemainingToBill() {
        KualiDecimal total = KualiDecimal.ZERO;
        total = getAwardTotal().subtract(getTotalAmountBilledToDate());
        return total;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public KualiDecimal getTotalPreviouslyBilled() {
        return totalPreviouslyBilled;
    }

    public void setTotalPreviouslyBilled(KualiDecimal totalPreviouslyBilled) {
        this.totalPreviouslyBilled = totalPreviouslyBilled;
    }

    public KualiDecimal getCostShareAmount() {
        return costShareAmount;
    }

    public void setCostShareAmount(KualiDecimal costShareAmount) {
        this.costShareAmount = costShareAmount;
    }

    public ContractsGrantsInvoiceDocument getInvoiceDocument() {
        return invoiceDocument;
    }

    public void setInvoiceDocument(ContractsGrantsInvoiceDocument invoiceDocument) {
        this.invoiceDocument = invoiceDocument;
    }

    public Date getLastBilledDate() {
        return lastBilledDate;
    }

    public void setLastBilledDate(Date lastBilledDate) {
        this.lastBilledDate = lastBilledDate;
    }

    public String getDunningLetterTemplateAssigned() {
        return dunningLetterTemplateAssigned;
    }

    public void setDunningLetterTemplateAssigned(String dunningLetterTemplateAssigned) {
        this.dunningLetterTemplateAssigned = dunningLetterTemplateAssigned;
    }

    public Date getDunningLetterTemplateSentDate() {
        return dunningLetterTemplateSentDate;
    }

    public void setDunningLetterTemplateSentDate(Date dunningLetterTemplateSentDate) {
        this.dunningLetterTemplateSentDate = dunningLetterTemplateSentDate;
    }

    public String getProposalNumber() {
        return proposalNumber;
    }

    public void setProposalNumber(String proposalNumber) {
        this.proposalNumber = proposalNumber;
    }

    public String getAwardNumber() {
        return awardNumber;
    }

    public void setAwardNumber(String awardNumber) {
        this.awardNumber = awardNumber;
    }

    public Integer getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(Integer sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public String getLetterOfCreditCreationType() {
        return letterOfCreditCreationType;
    }

    public void setLetterOfCreditCreationType(String letterOfCreditCreationType) {
        this.letterOfCreditCreationType = letterOfCreditCreationType;
    }

    public String getLetterOfCreditFundGroupCode() {
        return letterOfCreditFundGroupCode;
    }

    public void setLetterOfCreditFundGroupCode(String letterOfCreditFundGroupCode) {
        this.letterOfCreditFundGroupCode = letterOfCreditFundGroupCode;
    }

    public String getLetterOfCreditFundCode() {
        return letterOfCreditFundCode;
    }

    public void setLetterOfCreditFundCode(String letterOfCreditFundCode) {
        this.letterOfCreditFundCode = letterOfCreditFundCode;
    }

    public ContractsAndGrantsBillingAward getAward() {
        final ContractsAndGrantsBillingAward updatedAward = getContractsAndGrantsModuleBillingService().
                updateAwardIfNecessary(proposalNumber, award);

        // If the updated award is null, we return the original award instead so that we don't get an NPE in the case
        // where it has been set by PojoPropertyUtilsBean.
        if (ObjectUtils.isNull(updatedAward)) {
            return award;
        }
        return updatedAward;
    }

    public void setAward(ContractsAndGrantsBillingAward award) {
        this.award = award;
    }

    public KualiModuleService getKualiModuleService() {
        if (kualiModuleService == null) {
            kualiModuleService = SpringContext.getBean(KualiModuleService.class);
        }
        return kualiModuleService;
    }

    public void setKualiModuleService(KualiModuleService kualiModuleService) {
        this.kualiModuleService = kualiModuleService;
    }

    protected ContractsAndGrantsModuleBillingService getContractsAndGrantsModuleBillingService() {
        if (contractsAndGrantsModuleBillingService == null) {
            contractsAndGrantsModuleBillingService = SpringContext.getBean(ContractsAndGrantsModuleBillingService.class);
        }
        return contractsAndGrantsModuleBillingService;
    }

    protected void setContractsAndGrantsModuleBillingService(
            ContractsAndGrantsModuleBillingService contractsAndGrantsModuleBillingService) {
        this.contractsAndGrantsModuleBillingService = contractsAndGrantsModuleBillingService;
    }
}
