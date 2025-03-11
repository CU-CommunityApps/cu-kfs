package edu.cornell.kfs.tax.fixture;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import edu.cornell.kfs.sys.util.FixtureUtils;
import edu.cornell.kfs.tax.batch.xml.TaxOutputSectionV2;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface TaxOutputSectionFixture {

    String name();

    boolean useExactFieldLengths() default false;

    TaxOutputFieldFixture[] fields();



    public static final class Utils {
        public static TaxOutputSectionV2 toDTO(final TaxOutputSectionFixture fixture) {
            final TaxOutputSectionV2 taxSection = new TaxOutputSectionV2();
            taxSection.setName(fixture.name());
            taxSection.setUseExactFieldLengths(fixture.useExactFieldLengths());
            taxSection.setFields(
                    FixtureUtils.convertFixtures(TaxOutputFieldFixture.Utils::toDTO, fixture.fields()));
            return taxSection;
        }
    }

}
