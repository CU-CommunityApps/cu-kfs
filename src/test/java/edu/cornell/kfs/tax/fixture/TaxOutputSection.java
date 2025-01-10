package edu.cornell.kfs.tax.fixture;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import edu.cornell.kfs.sys.util.FixtureUtils;
import edu.cornell.kfs.tax.batch.xml.TaxOutputSectionV2;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface TaxOutputSection {

    String name();

    boolean hasHeaderRow() default false;

    boolean useExactFieldLengths() default false;

    TaxOutputField[] fields();



    public static final class Utils {
        public static TaxOutputSectionV2 toDTO(final TaxOutputSection fixture) {
            final TaxOutputSectionV2 taxSection = new TaxOutputSectionV2();
            taxSection.setName(fixture.name());
            taxSection.setHasHeaderRow(fixture.hasHeaderRow());
            taxSection.setUseExactFieldLengths(fixture.useExactFieldLengths());
            taxSection.setFields(
                    FixtureUtils.convertFixtures(TaxOutputField.Utils::toDTO, fixture.fields()));
            return taxSection;
        }
    }

}
