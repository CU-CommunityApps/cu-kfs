package edu.cornell.kfs.tax.fixture;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.commons.lang3.StringUtils;

import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.tax.batch.CUTaxBatchConstants.TaxOutputFieldType;
import edu.cornell.kfs.tax.batch.xml.TaxOutputFieldV2;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface TaxOutputFieldFixture {

    String name();

    int length();

    TaxOutputFieldType type();

    String key() default CUKFSConstants.NULL;

    String value() default CUKFSConstants.NULL;



    public static final class Utils {
        public static TaxOutputFieldV2 toDTO(final TaxOutputFieldFixture fixture) {
            final TaxOutputFieldV2 taxField = new TaxOutputFieldV2();
            taxField.setName(fixture.name());
            taxField.setLength(fixture.length());
            taxField.setType(fixture.type());
            taxField.setKey(convertNullWordToNullStringIfPresent(fixture.key()));
            taxField.setValue(convertNullWordToNullStringIfPresent(fixture.value()));
            return taxField;
        }

        private static String convertNullWordToNullStringIfPresent(final String value) {
            return StringUtils.equalsIgnoreCase(value, CUKFSConstants.NULL) ? null : value;
        }
    }

}
