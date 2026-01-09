package edu.cornell.kfs.coa.rest.resource.fixture;

import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.kns.datadictionary.validation.charlevel.AlphaNumericValidationPattern;
import org.kuali.kfs.kns.datadictionary.validation.charlevel.AlphaValidationPattern;
import org.kuali.kfs.kns.datadictionary.validation.charlevel.AnyCharacterValidationPattern;
import org.kuali.kfs.krad.bo.Attachment;

import edu.cornell.kfs.krad.fixture.AttributeDefinitionFixture;
import edu.cornell.kfs.krad.fixture.BusinessObjectEntryFixture;
import edu.cornell.kfs.krad.fixture.ValidationPatternFixture;

public enum AccountAttachmentControllerTestBoEntry {

    @BusinessObjectEntryFixture(
        businessObjectClass = Account.class,
        attributes = {
            @AttributeDefinitionFixture(
                name = "chartOfAccountsCode",
                label = "Chart Code",
                maxLength = 2,
                validationPattern = @ValidationPatternFixture(
                    type = AlphaValidationPattern.class,
                    exactLength = 2
                )
            ),
            @AttributeDefinitionFixture(
                name = "accountNumber",
                label = "Account Number",
                maxLength = 7,
                validationPattern = @ValidationPatternFixture(
                    type = AlphaNumericValidationPattern.class,
                    exactLength = 7
                )
            )
        }
    )
    ACCOUNT,

    @BusinessObjectEntryFixture(
        businessObjectClass = Attachment.class,
        attributes = {
            @AttributeDefinitionFixture(
                name = "attachmentIdentifier",
                label = "Attachment Identifier",
                maxLength = 36,
                validationPattern = @ValidationPatternFixture(type = AnyCharacterValidationPattern.class)
            )
        }
    )
    ATTACHMENT;

}
