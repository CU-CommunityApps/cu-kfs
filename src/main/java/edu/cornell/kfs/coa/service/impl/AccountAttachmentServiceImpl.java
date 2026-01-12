package edu.cornell.kfs.coa.service.impl;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.service.AccountService;
import org.kuali.kfs.krad.bo.Attachment;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import edu.cornell.kfs.coa.rest.jsonObjects.AccountAttachmentErrorResponseDto;
import edu.cornell.kfs.coa.rest.jsonObjects.AccountAttachmentListItemDto;
import edu.cornell.kfs.coa.rest.jsonObjects.AccountAttachmentListingDto;
import edu.cornell.kfs.coa.service.AccountAttachmentService;
import edu.cornell.kfs.krad.CUKRADPropertyConstants;
import edu.cornell.kfs.sys.service.WebApiParameterValidationService;
import edu.cornell.kfs.sys.util.WebApiParameter;
import edu.cornell.kfs.sys.web.CuResponseStatusException;

public class AccountAttachmentServiceImpl implements AccountAttachmentService {

    private AccountService accountService;
    private WebApiParameterValidationService webApiParameterValidationService;

    @Override
    public AccountAttachmentListingDto getAccountAttachmentListing(final String chartCode, final String accountNumber) {
        validateInputs(
                WebApiParameter.required(Account.class, KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, chartCode),
                WebApiParameter.required(Account.class, KFSPropertyConstants.ACCOUNT_NUMBER, accountNumber));

        final Account account = getExistingAccount(chartCode, accountNumber);
        return createAccountAttachmentListing(account);
    }

    private Account getExistingAccount(final String chartCode, final String accountNumber) {
        final Account account = accountService.getByPrimaryId(chartCode, accountNumber);
        if (ObjectUtils.isNull(account)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found");
        }
        return account;
    }

    private AccountAttachmentListingDto createAccountAttachmentListing(final Account account) {
        final AccountAttachmentListingDto attachmentListing = new AccountAttachmentListingDto();
        attachmentListing.setChartOfAccountsCode(account.getChartOfAccountsCode());
        attachmentListing.setAccountNumber(account.getAccountNumber());
        attachmentListing.setAccountName(account.getAccountName());
    
        final List<Note> accountNotes = account.getBoNotes();
        final List<AccountAttachmentListItemDto> attachments = accountNotes.stream()
                .filter(this::noteHasAttachment)
                .map(this::createAccountAttachmentListItem)
                .collect(Collectors.toUnmodifiableList());
        attachmentListing.setAttachments(attachments);

        return attachmentListing;
    }

    private boolean noteHasAttachment(final Note note) {
        return ObjectUtils.isNotNull(note.getAttachment());
    }

    private AccountAttachmentListItemDto createAccountAttachmentListItem(final Note note) {
        final AccountAttachmentListItemDto attachmentDto = new AccountAttachmentListItemDto();
        final Attachment attachment = note.getAttachment();
        Objects.requireNonNull(attachment, "Note's attachment should not be null");

        attachmentDto.setAttachmentId(attachment.getAttachmentIdentifier());
        attachmentDto.setAttachmentNote(note.getNoteText());
        attachmentDto.setFileName(attachment.getAttachmentFileName());
        attachmentDto.setMimeTypeCode(attachment.getAttachmentMimeTypeCode());
        attachmentDto.setFileSizeInBytes(attachment.getAttachmentFileSize());

        return attachmentDto;
    }

    @Override
    public Attachment getAccountAttachment(final String chartCode, final String accountNumber,
            final String attachmentId) {
        validateInputs(
                WebApiParameter.required(Account.class, KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, chartCode),
                WebApiParameter.required(Account.class, KFSPropertyConstants.ACCOUNT_NUMBER, accountNumber),
                WebApiParameter.required(Attachment.class, CUKRADPropertyConstants.ATTACHMENT_IDENTIFIER, attachmentId));

        final Account account = getExistingAccount(chartCode, accountNumber);
        final List<Note> accountNotes = account.getBoNotes();
        final Attachment matchingAttachment = accountNotes.stream()
                .filter(this::noteHasAttachment)
                .map(Note::getAttachment)
                .filter(attachment -> StringUtils.equals(attachment.getAttachmentIdentifier(), attachmentId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Attachment not found"));

        return matchingAttachment;
    }

    private void validateInputs(final WebApiParameter... parameters) {
        final List<String> errors = webApiParameterValidationService.validateParameters(parameters);
        if (!errors.isEmpty()) {
            throw new CuResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid search parameters",
                    AccountAttachmentErrorResponseDto.of(errors));
        }
    }

    public void setAccountService(final AccountService accountService) {
        this.accountService = accountService;
    }

    public void setWebApiParameterValidationService(
            final WebApiParameterValidationService webApiParameterValidationService) {
        this.webApiParameterValidationService = webApiParameterValidationService;
    }

}
