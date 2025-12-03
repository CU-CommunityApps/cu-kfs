package edu.cornell.kfs.coa.service.impl;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ContextedRuntimeException;
import org.apache.commons.lang3.tuple.Pair;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.service.AccountService;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.krad.bo.Attachment;
import org.kuali.kfs.krad.bo.BusinessObject;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.datadictionary.AttributeDefinition;
import org.kuali.kfs.krad.datadictionary.validation.ValidationPattern;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import edu.cornell.kfs.coa.rest.jsonObjects.AccountAttachmentListItemDto;
import edu.cornell.kfs.coa.rest.jsonObjects.AccountAttachmentListingDto;
import edu.cornell.kfs.coa.service.AccountAttachmentControllerHelperService;
import edu.cornell.kfs.krad.CUKRADPropertyConstants;
import edu.cornell.kfs.sys.CUKFSPropertyConstants;

@SuppressWarnings("deprecation")
public class AccountAttachmentControllerHelperServiceImpl implements AccountAttachmentControllerHelperService {

    private static final Map<String, Class<? extends BusinessObject>> PROPERTY_TO_BO_MAPPINGS = Map.ofEntries(
            Map.entry(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, Account.class),
            Map.entry(KFSPropertyConstants.ACCOUNT_NUMBER, Account.class),
            Map.entry(CUKRADPropertyConstants.ATTACHMENT_IDENTIFIER, Attachment.class)
    );

    private AccountService accountService;
    private DataDictionaryService dataDictionaryService;
    private ConfigurationService configurationService;

    @Override
    public AccountAttachmentListingDto getAccountAttachmentListing(final String chartCode, final String accountNumber) {
        validatePropertyValues(
                Pair.of(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, chartCode),
                Pair.of(KFSPropertyConstants.ACCOUNT_NUMBER, accountNumber));

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
                .filter(note -> ObjectUtils.isNotNull(note.getAttachment()))
                .map(this::createAccountAttachmentListItem)
                .collect(Collectors.toUnmodifiableList());
        attachmentListing.setAttachments(attachments);

        return attachmentListing;
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
        validatePropertyValues(
                Pair.of(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, chartCode),
                Pair.of(KFSPropertyConstants.ACCOUNT_NUMBER, accountNumber),
                Pair.of(CUKRADPropertyConstants.ATTACHMENT_IDENTIFIER, attachmentId));

        final Account account = getExistingAccount(chartCode, accountNumber);
        final List<Note> accountNotes = account.getBoNotes();
        final Attachment matchingAttachment = accountNotes.stream()
                .map(Note::getAttachment)
                .filter(ObjectUtils::isNotNull)
                .filter(attachment -> StringUtils.equals(attachment.getAttachmentIdentifier(), attachmentId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Attachment not found"));

        return matchingAttachment;
    }

    @SafeVarargs
    private void validatePropertyValues(final Pair<String, String>... properties) {
        List<String> errors = Stream.of(properties)
                .flatMap(this::validatePropertyValue)
                .collect(Collectors.toUnmodifiableList());
        if (!errors.isEmpty()) {
            throw createContextedRuntimeException(HttpStatus.BAD_REQUEST, "Invalid search parameters", errors);
        }
    }

    private Stream<String> validatePropertyValue(final Pair<String, String> property) {
        return validatePropertyValue(property.getLeft(), property.getRight());
    }

    private Stream<String> validatePropertyValue(final String propertyName, final String propertyValue) {
        final Stream.Builder<String> errors = Stream.builder();
        final Class<? extends BusinessObject> boClass = PROPERTY_TO_BO_MAPPINGS.get(propertyName);
        Objects.requireNonNull(boClass, "Unexpected null BO class for BO property " + propertyName);
        final AttributeDefinition attribute = dataDictionaryService.getAttributeDefinition(
                boClass.getSimpleName(), propertyName);
        Objects.requireNonNull(attribute, "Unexpected null data dictionary attribute for " + propertyName);

        if (StringUtils.isBlank(propertyValue)) {
            errors.add(createErrorMessage(KFSKeyConstants.ERROR_REQUIRED, attribute.getLabel()));
            return errors.build();
        }

        final Integer maxLength = attribute.getMaxLength();
        Objects.requireNonNull(maxLength, "Unexpected null max length for " + attribute.getName());
        if (propertyValue.length() > attribute.getMaxLength()) {
            errors.add(createErrorMessage(KFSKeyConstants.ERROR_MAX_LENGTH,
                    attribute.getLabel(), attribute.getMaxLength()));
        }

        final ValidationPattern validationPattern = attribute.getValidationPattern();
        Objects.requireNonNull(validationPattern, "Unexpected null validation pattern for " + attribute.getName());
        if (!validationPattern.matches(propertyValue)) {
            errors.add(createErrorMessage(validationPattern.getValidationErrorMessageKey(), attribute.getLabel()));
        }

        return errors.build();
    }

    private String createErrorMessage(final String patternKey, final Object... arguments) {
        final String pattern = configurationService.getPropertyValueAsString(patternKey);
        return MessageFormat.format(pattern, arguments);
    }

    private ContextedRuntimeException createContextedRuntimeException(
            final HttpStatus httpStatus, final String message, final List<String> errors) {
        ContextedRuntimeException exception = new ContextedRuntimeException(message)
                .addContextValue(CUKFSPropertyConstants.STATUS, httpStatus);
        for (final String error : errors) {
            exception = exception.addContextValue(CUKFSPropertyConstants.ERRORS, error);
        }
        return exception;
    }

}
