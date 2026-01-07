package edu.cornell.kfs.krad.fixture;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.kuali.kfs.krad.datadictionary.AttributeDefinition;
import org.kuali.kfs.krad.datadictionary.validation.ValidationPattern;

@SuppressWarnings("deprecation")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface AttributeDefinitionFixture {

    String name();
    String label();
    int maxLength() default -1;
    ValidationPatternFixture validationPattern()
            default @ValidationPatternFixture(type = ValidationPattern.class);

    public static final class Utils {

        public static AttributeDefinition toAttributeDefinition(final AttributeDefinitionFixture fixture) {
            final AttributeDefinition attribute = new AttributeDefinition();
            attribute.setName(fixture.name());
            attribute.setLabel(fixture.label());
            if (fixture.maxLength() != -1) {
                attribute.setMaxLength(fixture.maxLength());
            }

            ValidationPattern pattern = ValidationPatternFixture.Utils.toValidationPattern(fixture.validationPattern());
            if (pattern != null) {
                attribute.setValidationPattern(pattern);
            }

            return attribute;
        }

    }

}
