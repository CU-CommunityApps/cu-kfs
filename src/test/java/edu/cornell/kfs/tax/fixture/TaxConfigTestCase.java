package edu.cornell.kfs.tax.fixture;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import edu.cornell.kfs.sys.util.TestDateUtils;
import edu.cornell.kfs.tax.batch.TaxBatchConfig;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface TaxConfigTestCase {

    String datesToProcessSetting();

    String taxType();

    int reportYear();

    String processingStartDate();

    String taxDateRangeStart();

    String taxDateRangeEnd();



    public static final class Utils {
        public static TaxBatchConfig toTaxBatchConfig(final TaxConfigTestCase testCase) {
            return new TaxBatchConfig(
                    testCase.taxType(),
                    testCase.reportYear(),
                    TestDateUtils.toUtilDate(testCase.processingStartDate()),
                    TestDateUtils.toSqlDate(testCase.taxDateRangeStart()),
                    TestDateUtils.toSqlDate(testCase.taxDateRangeEnd()));
        }
    }

}
