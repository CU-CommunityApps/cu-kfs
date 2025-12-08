package edu.cornell.kfs.coa.rest.resource.fixture;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface AccountAttachmentListingFixture {

    String chartOfAccountsCode();
    String accountNumber();
    String accountName();
    AccountAttachmentNoteFixture[] notes();

}
