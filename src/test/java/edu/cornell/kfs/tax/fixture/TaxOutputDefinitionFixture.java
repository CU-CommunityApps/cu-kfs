package edu.cornell.kfs.tax.fixture;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import edu.cornell.kfs.sys.util.FixtureUtils;
import edu.cornell.kfs.tax.batch.xml.TaxOutputDefinitionV2;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface TaxOutputDefinitionFixture {

    String fieldSeparator();

    boolean includeQuotes() default true;

    TaxOutputSectionFixture[] sections();



    public static final class Utils {
        public static TaxOutputDefinitionV2 toDTO(final TaxOutputDefinitionFixture fixture) {
            final TaxOutputDefinitionV2 outputDefinition = new TaxOutputDefinitionV2();
            outputDefinition.setFieldSeparator(fixture.fieldSeparator());
            outputDefinition.setIncludeQuotes(fixture.includeQuotes());
            outputDefinition.setSections(
                    FixtureUtils.convertFixtures(TaxOutputSectionFixture.Utils::toDTO, fixture.sections()));
            return outputDefinition;
        }
    }

}
