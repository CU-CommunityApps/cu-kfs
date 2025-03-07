package edu.cornell.kfs.tax.fixture;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.util.FixtureUtils;
import edu.cornell.kfs.tax.batch.xml.TaxOutputDefinitionV2;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface TaxOutputDefinitionFixture {

    String fieldSeparator();

    String amountFormat() default CUKFSConstants.NULL;

    String percentFormat() default CUKFSConstants.NULL;

    boolean includeQuotes() default true;

    TaxOutputSectionFixture[] sections();



    public static final class Utils {
        public static TaxOutputDefinitionV2 toDTO(final TaxOutputDefinitionFixture fixture) {
            final TaxOutputDefinitionV2 outputDefinition = new TaxOutputDefinitionV2();
            outputDefinition.setFieldSeparator(fixture.fieldSeparator());
            outputDefinition.setAmountFormat(FixtureUtils.convertToNullIfEqualToTheWordNull(fixture.amountFormat()));
            outputDefinition.setPercentFormat(FixtureUtils.convertToNullIfEqualToTheWordNull(fixture.percentFormat()));
            outputDefinition.setIncludeQuotes(fixture.includeQuotes());
            outputDefinition.setSections(
                    FixtureUtils.convertFixtures(TaxOutputSectionFixture.Utils::toDTO, fixture.sections()));
            return outputDefinition;
        }
    }

}
