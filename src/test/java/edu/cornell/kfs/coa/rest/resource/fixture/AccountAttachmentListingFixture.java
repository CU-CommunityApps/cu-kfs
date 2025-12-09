package edu.cornell.kfs.coa.rest.resource.fixture;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.kuali.kfs.coa.businessobject.Account;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface AccountAttachmentListingFixture {

    String chartOfAccountsCode();
    String accountNumber();
    String accountName();
    AccountAttachmentNoteFixture[] notes();

    public static final class Utils {

        public static Account toAccount(final AccountAttachmentListingFixture fixture) {
            final Account account = new Account();
            return account;
        }

    }

}
