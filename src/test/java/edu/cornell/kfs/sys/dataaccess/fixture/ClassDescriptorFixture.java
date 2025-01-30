package edu.cornell.kfs.sys.dataaccess.fixture;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

import org.apache.ojb.broker.metadata.ClassDescriptor;

import edu.cornell.kfs.sys.dataaccess.util.TestOjbMetadataUtils;

@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface ClassDescriptorFixture {

    Class<?> mappedClass();

    String table();

    FieldDescriptorFixture[] fieldDescriptors();



    public static final class Utils {
        public static ClassDescriptor toOjbClassDescriptor(final ClassDescriptorFixture fixture) {
            return TestOjbMetadataUtils.createMockClassDescriptor(fixture.mappedClass(), fixture.table(),
                    List.of(fixture.fieldDescriptors()), FieldDescriptorFixture.Utils::toOjbFieldDescriptor);
        }
    }

}
