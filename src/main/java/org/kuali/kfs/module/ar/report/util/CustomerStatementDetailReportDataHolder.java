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
package org.kuali.kfs.module.ar.report.util;

import org.kuali.kfs.coa.businessobject.Organization;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.ar.ArConstants;
import org.kuali.kfs.module.ar.businessobject.SystemInformation;
import org.kuali.kfs.sys.businessobject.DocumentHeader;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.UniversityDateService;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;

import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

public class CustomerStatementDetailReportDataHolder {

    private String documentNumber;
    private Date documentFinalDate;
    private String documentFinalDateString;
    private String documentDescription;
    private KualiDecimal financialDocumentTotalAmountCharge;
    private KualiDecimal financialDocumentTotalAmountCredit;
    private String orgName;
    private String fein;
    private String docType;

    public CustomerStatementDetailReportDataHolder(String description, KualiDecimal totalAmount) {
        this.docType = "";
        this.documentDescription = description;
        this.financialDocumentTotalAmountCharge = totalAmount;
    }

    public CustomerStatementDetailReportDataHolder(DocumentHeader docHeader, Organization processingOrg,
                                                   String docType, KualiDecimal totalAmount) {
        documentDescription = docHeader.getDocumentDescription();
        if (docType.equals(ArConstants.INVOICE_DOC_TYPE)) {
            financialDocumentTotalAmountCharge = totalAmount;
        } else {
            financialDocumentTotalAmountCredit = totalAmount;
        }

        documentNumber = docHeader.getDocumentNumber();
        if (ObjectUtils.isNotNull(docHeader.getWorkflowDocument().getDateApproved())) {
            // CU Customization: Commented this out to get things to compile
            //java.util.Date lastApprovedDate = docHeader.getWorkflowDocument().getDateApproved().toDate();
            //this.setDocumentFinalDate(new java.sql.Date(lastApprovedDate.getTime()));
        }
        this.docType = docType;

        orgName = processingOrg.getOrganizationName();

        String fiscalYear = SpringContext.getBean(UniversityDateService.class).getCurrentFiscalYear().toString();
        Map<String, String> criteria = new HashMap<>();
        criteria.put("universityFiscalYear", fiscalYear);
        criteria.put("processingChartOfAccountCode", processingOrg.getChartOfAccountsCode());
        criteria.put("processingOrganizationCode", processingOrg.getOrganizationCode());

        SystemInformation sysinfo = SpringContext.getBean(BusinessObjectService.class).findByPrimaryKey(
                SystemInformation.class, criteria);
        if (sysinfo == null) {
            fein = null;
        } else {
            fein = sysinfo.getUniversityFederalEmployerIdentificationNumber();
        }
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public Date getDocumentFinalDate() {
        return documentFinalDate;
    }

    /**
     * This method formats the date value into a string that can then be used
     *
     * @return
     */
    public String getDocumentFinalDateString() {
        return documentFinalDateString;
    }

    public void setDocumentFinalDate(Date documentFinalDate) {
        this.documentFinalDate = documentFinalDate;
        if (documentFinalDate == null) {
            return;
        }
        this.documentFinalDateString = SpringContext.getBean(DateTimeService.class).toDateString(documentFinalDate);
    }

    public String getDocumentDescription() {
        return documentDescription;
    }

    public void setDocumentDescription(String documentDescription) {
        this.documentDescription = documentDescription;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getFein() {
        return fein;
    }

    public void setFein(String fein) {
        this.fein = fein;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public KualiDecimal getFinancialDocumentTotalAmountCharge() {
        return financialDocumentTotalAmountCharge;
    }

    public void setFinancialDocumentTotalAmountCharge(KualiDecimal financialDocumentTotalAmountCharge) {
        this.financialDocumentTotalAmountCharge = financialDocumentTotalAmountCharge;
    }

    public KualiDecimal getFinancialDocumentTotalAmountCredit() {
        return financialDocumentTotalAmountCredit;
    }

    public void setFinancialDocumentTotalAmountCredit(KualiDecimal financialDocumentTotalAmountCredit) {
        this.financialDocumentTotalAmountCredit = financialDocumentTotalAmountCredit;
    }

}
