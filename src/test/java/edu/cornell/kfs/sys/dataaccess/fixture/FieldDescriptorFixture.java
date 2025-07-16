package edu.cornell.kfs.sys.dataaccess.fixture;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.sql.JDBCType;

import org.apache.ojb.broker.accesslayer.conversions.FieldConversion;
import org.apache.ojb.broker.accesslayer.conversions.FieldConversionDefaultImpl;
import org.apache.ojb.broker.metadata.FieldDescriptor;

import edu.cornell.kfs.sys.dataaccess.util.TestOjbMetadataUtils;

@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface FieldDescriptorFixture {

    String name();

    String column();

    JDBCType jdbcType();

    Class<? extends FieldConversion> conversion() default FieldConversionDefaultImpl.class;

    public static final class Utils {
        public static FieldDescriptor toOjbFieldDescriptor(final FieldDescriptorFixture fixture) {
            return TestOjbMetadataUtils.createMockFieldDescriptor(fixture.name(), fixture.column(),
                    fixture.jdbcType(), fixture.conversion());
        }
    }

}
