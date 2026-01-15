package edu.cornell.kfs.coa.rest.resource.fixture;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.krad.bo.Note;

import edu.cornell.kfs.coa.rest.jsonObjects.AccountAttachmentListItemDto;
import edu.cornell.kfs.coa.rest.jsonObjects.AccountAttachmentListingDto;
import edu.cornell.kfs.sys.util.CuMockBuilder;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface AccountAttachmentListingFixture {

    String chartOfAccountsCode();
    String accountNumber();
    String accountName();
    AccountAttachmentNoteFixture[] notes();

    public static final class Utils {

        public static Account toAccountWithNotesList(final AccountAttachmentListingFixture fixture) {
            final Account account = new Account();
            account.setChartOfAccountsCode(fixture.chartOfAccountsCode());
            account.setAccountNumber(fixture.accountNumber());
            account.setAccountName(fixture.accountName());

            return new CuMockBuilder<>(account)
                    .withAnswer(Account::getBoNotes, invocation -> toBoNotesList(fixture))
                    .build();
        }

        public static List<Note> toBoNotesList(final AccountAttachmentListingFixture fixture) {
            return Arrays.stream(fixture.notes())
                    .map(AccountAttachmentNoteFixture.Utils::toNote)
                    .collect(Collectors.toUnmodifiableList());
        }

        public static AccountAttachmentListingDto toAccountAttachmentListingDto(
                final AccountAttachmentListingFixture fixture) {
            final AccountAttachmentListingDto dto = new AccountAttachmentListingDto();
            dto.setChartOfAccountsCode(fixture.chartOfAccountsCode());
            dto.setAccountNumber(fixture.accountNumber());
            dto.setAccountName(fixture.accountName());
            dto.setAttachments(createListOfAccountAttachmentDtos(fixture));
            return dto;
        }

        private static List<AccountAttachmentListItemDto> createListOfAccountAttachmentDtos(
                final AccountAttachmentListingFixture fixture) {
            return Arrays.stream(fixture.notes())
                    .filter(AccountAttachmentNoteFixture::hasAttachment)
                    .map(AccountAttachmentNoteFixture.Utils::toAccountAttachmentListItemDto)
                    .collect(Collectors.toUnmodifiableList());
        }

    }

}
