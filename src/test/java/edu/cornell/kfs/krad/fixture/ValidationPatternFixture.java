package edu.cornell.kfs.krad.fixture;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.lang3.Validate;
import org.kuali.kfs.kns.datadictionary.validation.charlevel.AlphaNumericValidationPattern;
import org.kuali.kfs.kns.datadictionary.validation.charlevel.AlphaValidationPattern;
import org.kuali.kfs.kns.datadictionary.validation.charlevel.AnyCharacterValidationPattern;
import org.kuali.kfs.krad.datadictionary.validation.CharacterLevelValidationPattern;
import org.kuali.kfs.krad.datadictionary.validation.ValidationPattern;

@SuppressWarnings("deprecation")
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface ValidationPatternFixture {

    Class<? extends ValidationPattern> type();
    int maxLength() default -1;
    int exactLength() default -1;
    boolean allowWhitespace() default false;

    public static final class Utils {

        private static final Map<Class<?>, Function<ValidationPatternFixture, ValidationPattern>> PATTERN_FACTORIES
                = Map.ofEntries(
                        Map.entry(AlphaValidationPattern.class, Utils::createAlphaValidationPattern),
                        Map.entry(AlphaNumericValidationPattern.class, Utils::createAlphaNumericValidationPattern),
                        Map.entry(AnyCharacterValidationPattern.class, Utils::createAnyCharacterValidationPattern)
                );

        public static ValidationPattern toValidationPattern(final ValidationPatternFixture fixture) {
            Objects.requireNonNull(fixture, "fixture cannot be null");
            Validate.isTrue(PATTERN_FACTORIES.containsKey(fixture.type()),
                    "Unsupported ValidationPattern implementation: " + fixture.type());
            return PATTERN_FACTORIES.get(fixture.type()).apply(fixture);
        }

        private static AlphaValidationPattern createAlphaValidationPattern(final ValidationPatternFixture fixture) {
            final AlphaValidationPattern pattern = createBaseCharValidationPattern(fixture, AlphaValidationPattern::new);
            pattern.setAllowWhitespace(fixture.allowWhitespace());
            return pattern;
        }

        private static AlphaNumericValidationPattern createAlphaNumericValidationPattern(
                final ValidationPatternFixture fixture) {
            final AlphaNumericValidationPattern pattern = createBaseCharValidationPattern(
                    fixture, AlphaNumericValidationPattern::new);
            pattern.setAllowWhitespace(fixture.allowWhitespace());
            return pattern;
        }

        private static AnyCharacterValidationPattern createAnyCharacterValidationPattern(
                final ValidationPatternFixture fixture) {
            final AnyCharacterValidationPattern pattern = createBaseCharValidationPattern(
                    fixture, AnyCharacterValidationPattern::new);
            pattern.setAllowWhitespace(fixture.allowWhitespace());
            return pattern;
        }

        private static <T extends CharacterLevelValidationPattern> T createBaseCharValidationPattern(
                final ValidationPatternFixture fixture, final Supplier<T> validationPatternConstructor) {
            final T pattern = validationPatternConstructor.get();
            pattern.setMaxLength(fixture.maxLength());
            pattern.setExactLength(fixture.exactLength());
            return pattern;
        }
    }

}
