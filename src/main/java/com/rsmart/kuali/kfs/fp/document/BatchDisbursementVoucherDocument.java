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
package com.rsmart.kuali.kfs.fp.document;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.exception.ParseException;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.core.web.format.BooleanFormatter;
import org.kuali.kfs.krad.bo.Attachment;


/**
 * Provides String setter methods and additional properties for population from XML (batch)
 */
public class BatchDisbursementVoucherDocument extends DisbursementVoucherDocument {

    private static final long serialVersionUID = 1L;
    private List<Attachment> attachments;

    public BatchDisbursementVoucherDocument() {
        super();
        attachments = new ArrayList<Attachment>();
    }

    /**
     * Takes a <code>String</code> and attempt to format as <code>Date</code> for setting the disbursementVoucherDueDate field
     * 
     * @param disbursementVoucherDueDate Date as string
     */
    public void setDisbursementVoucherDueDate(String disbursementVoucherDueDate) {
        try {
            super.setDisbursementVoucherDueDate(SpringContext.getBean(DateTimeService.class).convertToSqlDate(disbursementVoucherDueDate));
        }
        catch (java.text.ParseException e) {
            throw new ParseException("Unable to convert disbursement voucher due date value " + disbursementVoucherDueDate + " :" + e.getMessage(), e);
        }
    }

    /**
     * Takes a <code>String</code> and attempt to format as <code>KualiDecimal</code> for setting the disbVchrCheckTotalAmount field
     * 
     * @param disbVchrCheckTotalAmount as string
     */
    public void setDisbVchrCheckTotalAmount(String disbVchrCheckTotalAmount) {
        super.setDisbVchrCheckTotalAmount(new KualiDecimal(disbVchrCheckTotalAmount));
    }

    /**
     * Takes a <code>String</code> and attempt to format as <code>Boolean</code> for setting the disbVchrAttachmentCode field
     * 
     * @param disbVchrAttachmentCode as string
     */
    public void setDisbVchrAttachmentCode(String disbVchrAttachmentCode) {
        if (StringUtils.isNotBlank(disbVchrAttachmentCode)) {
            Boolean disbVchrAttachment = (Boolean) (new BooleanFormatter()).convertFromPresentationFormat(disbVchrAttachmentCode);
            super.setDisbVchrAttachmentCode(disbVchrAttachment);
        }
    }

    /**
     * Takes a <code>String</code> and attempt to format as <code>Boolean</code> for setting the disbVchrSpecialHandlingCode field
     * 
     * @param disbVchrSpecialHandlingCode as string
     */
    public void setDisbVchrSpecialHandlingCode(String disbVchrSpecialHandlingCode) {
        if (StringUtils.isNotBlank(disbVchrSpecialHandlingCode)) {
            Boolean disbVchrSpecialHandling = (Boolean) (new BooleanFormatter()).convertFromPresentationFormat(disbVchrSpecialHandlingCode);
            super.setDisbVchrSpecialHandlingCode(disbVchrSpecialHandling);
        }
    }
    
    /**
     * Takes a <code>String</code> and attempt to format as <code>Boolean</code> for setting the exceptionIndicator field
     * 
     * @param exceptionIndicator as string
     */
    public void setExceptionIndicator(String exceptionIndicator) {
        if (StringUtils.isNotBlank(exceptionIndicator)) {
            Boolean exception = (Boolean) (new BooleanFormatter()).convertFromPresentationFormat(exceptionIndicator);
            super.setExceptionIndicator(exception);
        }
    }

    /**
     * Gets the attachments attribute.
     * 
     * @return Returns the attachments.
     */
    public List<Attachment> getAttachments() {
        return attachments;
    }

    /**
     * Sets the attachments attribute value.
     * 
     * @param attachments The attachments to set.
     */
    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }

    public void addAttachment(Attachment attachment) {
        getAttachments().add(attachment);
    }

}
