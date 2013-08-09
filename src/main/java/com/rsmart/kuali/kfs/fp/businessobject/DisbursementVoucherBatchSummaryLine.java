/*
 * Copyright 2009 The Kuali Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rsmart.kuali.kfs.fp.businessobject;

import java.sql.Date;



/**
 * Holds information to produce audit report summary line
 */
public class DisbursementVoucherBatchSummaryLine {
    private String disbVchrCreateDate;
    private String disbursementVoucherDueDate;
    private String disbVchrPayeeId;
    private String disbVchrAmount;
    private String disbVchrPaymentReason;
    private String auditMessage;

    public DisbursementVoucherBatchSummaryLine() {
        super();
    }

    /**
     * @return Returns the disbVchrCreateDate.
     */
    public String getDisbVchrCreateDate() {
        return disbVchrCreateDate;
    }

    /**
     * @param disbVchrCreateDate The disbVchrCreateDate to set.
     */
    public void setDisbVchrCreateDate(String disbVchrCreateDate) {
        this.disbVchrCreateDate = disbVchrCreateDate;
    }

    /**
     * Gets the disbursementVoucherDueDate attribute.
     * 
     * @return Returns the disbursementVoucherDueDate.
     */
    public String getDisbursementVoucherDueDate() {
        return disbursementVoucherDueDate;
    }

    /**
     * Sets the disbursementVoucherDueDate attribute value.
     * 
     * @param disbursementVoucherDueDate The disbursementVoucherDueDate to set.
     */
    public void setDisbursementVoucherDueDate(String disbursementVoucherDueDate) {
        this.disbursementVoucherDueDate = disbursementVoucherDueDate;
    }

    /**
     * Gets the disbVchrPayeeId attribute.
     * 
     * @return Returns the disbVchrPayeeId.
     */
    public String getDisbVchrPayeeId() {
        return disbVchrPayeeId;
    }

    /**
     * Sets the disbVchrPayeeId attribute value.
     * 
     * @param disbVchrPayeeId The disbVchrPayeeId to set.
     */
    public void setDisbVchrPayeeId(String disbVchrPayeeId) {
        this.disbVchrPayeeId = disbVchrPayeeId;
    }

    /**
     * Gets the disbVchrAmount attribute.
     * 
     * @return Returns the disbVchrAmount.
     */
    public String getDisbVchrAmount() {
        return disbVchrAmount;
    }

    /**
     * Sets the disbVchrAmount attribute value.
     * 
     * @param disbVchrAmount The disbVchrAmount to set.
     */
    public void setDisbVchrAmount(String disbVchrAmount) {
        this.disbVchrAmount = disbVchrAmount;
    }

    /**
     * Gets the disbVchrPaymentReason attribute.
     * 
     * @return Returns the disbVchrPaymentReason.
     */
    public String getDisbVchrPaymentReason() {
        return disbVchrPaymentReason;
    }

    /**
     * Sets the disbVchrPaymentReason attribute value.
     * 
     * @param disbVchrPaymentReason The disbVchrPaymentReason to set.
     */
    public void setDisbVchrPaymentReason(String disbVchrPaymentReason) {
        this.disbVchrPaymentReason = disbVchrPaymentReason;
    }

    /**
     * Gets the auditMessage attribute.
     * 
     * @return Returns the auditMessage.
     */
    public String getAuditMessage() {
        return auditMessage;
    }

    /**
     * Sets the auditMessage attribute value.
     * 
     * @param auditMessage The auditMessage to set.
     */
    public void setAuditMessage(String auditMessage) {
        this.auditMessage = auditMessage;
    }

}
