package edu.cornell.kfs.vnd.batch.service.impl.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.vnd.batch.service.impl.fixture.VendorComparisonResultRowFixture;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface VendorComparisonResult {

    int index();

    boolean expectingSuccess() default true;

    boolean writeCsvHeaderLine() default true;

    String csvHeaderLineOverride() default KFSConstants.EMPTY_STRING;

    VendorComparisonResultRowFixture[] csvDataRows() default {};

    String expectedReportFile() default KFSConstants.EMPTY_STRING;

}
