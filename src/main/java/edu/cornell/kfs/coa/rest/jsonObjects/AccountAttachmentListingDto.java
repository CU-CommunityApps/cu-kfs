package edu.cornell.kfs.coa.rest.jsonObjects;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountAttachmentListingDto {

    private String chartOfAccountsCode;
    private String accountNumber;
    private String accountName;
    private List<AccountAttachmentListItemDto> attachments;

    public String getChartOfAccountsCode() {
        return chartOfAccountsCode;
    }

    public void setChartOfAccountsCode(final String chartOfAccountsCode) {
        this.chartOfAccountsCode = chartOfAccountsCode;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(final String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(final String accountName) {
        this.accountName = accountName;
    }

    public List<AccountAttachmentListItemDto> getAttachments() {
        if (attachments == null) {
            attachments = new ArrayList<>();
        }
        return attachments;
    }

    public void setAttachments(final List<AccountAttachmentListItemDto> attachments) {
        this.attachments = attachments;
    }

}
