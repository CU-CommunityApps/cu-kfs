package edu.cornell.kfs.concur.rest.jsonObjects;

import java.sql.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConcurV3ExpenseReportDTO{
    @JsonProperty("Name") 
    public String name;
    @JsonProperty("Total") 
    public double total;
    @JsonProperty("CurrencyCode") 
    public String currencyCode;
    @JsonProperty("Country") 
    public String country;
    @JsonProperty("CountrySubdivision") 
    public String countrySubdivision;
    @JsonProperty("CreateDate") 
    public Date createDate;
    @JsonProperty("SubmitDate") 
    public Date submitDate;
    @JsonProperty("ProcessingPaymentDate") 
    public Object processingPaymentDate;
    @JsonProperty("PaidDate") 
    public Object paidDate;
    @JsonProperty("ReceiptsReceived") 
    public boolean receiptsReceived;
    @JsonProperty("UserDefinedDate") 
    public Date userDefinedDate;
    @JsonProperty("LastComment") 
    public String lastComment;
    @JsonProperty("OwnerLoginID") 
    public String ownerLoginID;
    @JsonProperty("OwnerName") 
    public String ownerName;
    @JsonProperty("ApproverLoginID") 
    public Object approverLoginID;
    @JsonProperty("ApproverName") 
    public Object approverName;
    @JsonProperty("ApprovalStatusName") 
    public String approvalStatusName;
    @JsonProperty("ApprovalStatusCode") 
    public String approvalStatusCode;
    @JsonProperty("PaymentStatusName") 
    public String paymentStatusName;
    @JsonProperty("PaymentStatusCode") 
    public String paymentStatusCode;
    @JsonProperty("LastModifiedDate") 
    public Date lastModifiedDate;
    @JsonProperty("PersonalAmount") 
    public double personalAmount;
    @JsonProperty("AmountDueEmployee") 
    public double amountDueEmployee;
    @JsonProperty("AmountDueCompanyCard") 
    public double amountDueCompanyCard;
    @JsonProperty("TotalClaimedAmount") 
    public double totalClaimedAmount;
    @JsonProperty("TotalApprovedAmount") 
    public double totalApprovedAmount;
    @JsonProperty("LedgerName") 
    public String ledgerName;
    @JsonProperty("PolicyID") 
    public String policyID;
    @JsonProperty("EverSentBack") 
    public boolean everSentBack;
    @JsonProperty("HasException") 
    public boolean hasException;
    @JsonProperty("WorkflowActionUrl") 
    public String workflowActionUrl;
    @JsonProperty("OrgUnit1") 
    public ConcurV3ExpenseReportOrgUnitDTO orgUnit1;
    @JsonProperty("OrgUnit2") 
    public ConcurV3ExpenseReportOrgUnitDTO orgUnit2;
    @JsonProperty("OrgUnit3") 
    public ConcurV3ExpenseReportOrgUnitDTO orgUnit3;
    @JsonProperty("OrgUnit4") 
    public ConcurV3ExpenseReportOrgUnitDTO orgUnit4;
    @JsonProperty("OrgUnit5") 
    public ConcurV3ExpenseReportOrgUnitDTO orgUnit5;
    @JsonProperty("OrgUnit6") 
    public ConcurV3ExpenseReportOrgUnitDTO orgUnit6;
    @JsonProperty("Custom1") 
    public ConcurV3ExpenseReportCustomDTO custom1;
    @JsonProperty("Custom2") 
    public ConcurV3ExpenseReportCustomDTO custom2;
    @JsonProperty("Custom3") 
    public ConcurV3ExpenseReportCustomDTO custom3;
    @JsonProperty("Custom4") 
    public ConcurV3ExpenseReportCustomDTO custom4;
    @JsonProperty("Custom5") 
    public ConcurV3ExpenseReportCustomDTO custom5;
    @JsonProperty("Custom6") 
    public ConcurV3ExpenseReportCustomDTO custom6;
    @JsonProperty("Custom7") 
    public ConcurV3ExpenseReportCustomDTO custom7;
    @JsonProperty("Custom8") 
    public ConcurV3ExpenseReportCustomDTO custom8;
    @JsonProperty("Custom9") 
    public ConcurV3ExpenseReportCustomDTO custom9;
    @JsonProperty("Custom10") 
    public ConcurV3ExpenseReportCustomDTO custom10;
    @JsonProperty("Custom11") 
    public ConcurV3ExpenseReportCustomDTO custom11;
    @JsonProperty("Custom12") 
    public ConcurV3ExpenseReportCustomDTO custom12;
    @JsonProperty("Custom13") 
    public ConcurV3ExpenseReportCustomDTO custom13;
    @JsonProperty("Custom14") 
    public ConcurV3ExpenseReportCustomDTO custom14;
    @JsonProperty("Custom15") 
    public ConcurV3ExpenseReportCustomDTO custom15;
    @JsonProperty("Custom16") 
    public ConcurV3ExpenseReportCustomDTO custom16;
    @JsonProperty("Custom17") 
    public ConcurV3ExpenseReportCustomDTO custom17;
    @JsonProperty("Custom18") 
    public ConcurV3ExpenseReportCustomDTO custom18;
    @JsonProperty("Custom19") 
    public ConcurV3ExpenseReportCustomDTO custom19;
    @JsonProperty("Custom20") 
    public ConcurV3ExpenseReportCustomDTO custom20;
    @JsonProperty("ID") 
    public String iD;
    @JsonProperty("URI") 
    public String uRI;
}