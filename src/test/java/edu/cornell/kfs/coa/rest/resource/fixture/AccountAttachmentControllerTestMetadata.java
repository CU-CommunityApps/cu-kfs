package edu.cornell.kfs.coa.rest.resource.fixture;

import org.kuali.kfs.kns.datadictionary.validation.charlevel.AlphaNumericValidationPattern;
import org.kuali.kfs.kns.datadictionary.validation.charlevel.AlphaValidationPattern;
import org.kuali.kfs.kns.datadictionary.validation.charlevel.AnyCharacterValidationPattern;

import edu.cornell.kfs.krad.fixture.AttributeDefinitionFixture;
import edu.cornell.kfs.krad.fixture.ValidationPatternFixture;

public enum AccountAttachmentControllerTestMetadata {

    @AttributeDefinitionFixture(
        name = "chartOfAccountsCode",
        label = "Chart Code",
        maxLength = 2,
        validationPattern = @ValidationPatternFixture(
            type = AlphaValidationPattern.class,
            exactLength = 2
        )
    )
    CHART_OF_ACCOUNTS_CODE_ATTRIBUTE,

    @AttributeDefinitionFixture(
        name = "accountNumber",
        label = "Account Number",
        maxLength = 7,
        validationPattern = @ValidationPatternFixture(
            type = AlphaNumericValidationPattern.class,
            exactLength = 7
        )
    )
    ACCOUNT_NUMBER_ATTRIBUTE,

    @AttributeDefinitionFixture(
        name = "attachmentIdentifier",
        label = "Attachment Identifier",
        maxLength = 36,
        validationPattern = @ValidationPatternFixture(type = AnyCharacterValidationPattern.class)
    )
    ATTACHMENT_IDENTIFIER_ATTRIBUTE;

}
