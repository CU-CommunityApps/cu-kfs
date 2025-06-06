package edu.cornell.kfs.tax.fixture;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.sys.util.TestDateUtils;
import edu.cornell.kfs.tax.businessobject.TransactionOverride;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface TransactionOverrideFixture {

    String universityDate();
    String taxType();
    String documentNumber();
    int financialDocumentLineNumber();
    String boxNumber();
    String formType() default KFSConstants.EMPTY_STRING;
    boolean active() default true;

    public static final class Utils {
        public static TransactionOverride toTransactionOverride(final TransactionOverrideFixture fixture) {
            final TransactionOverride transactionOverride = new TransactionOverride();
            transactionOverride.setUniversityDate(TestDateUtils.toSqlDate(fixture.universityDate()));
            transactionOverride.setTaxType(fixture.taxType());
            transactionOverride.setDocumentNumber(fixture.documentNumber());
            transactionOverride.setFinancialDocumentLineNumber(fixture.financialDocumentLineNumber());
            transactionOverride.setBoxNumber(fixture.boxNumber());
            transactionOverride.setFormType(StringUtils.defaultIfBlank(fixture.formType(), null));
            transactionOverride.setActive(fixture.active());
            return transactionOverride;
        }
    }

}
