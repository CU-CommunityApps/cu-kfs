package edu.cornell.kfs.sys.dataaccess.fixture;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

import org.apache.ojb.broker.metadata.ClassDescriptor;

import edu.cornell.kfs.sys.dataaccess.util.TestOjbMetadataUtils;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ClassDescriptorFixture {

    Class<?> mappedClass();

    String table();

    FieldDescriptorFixture[] fieldDescriptors();

    public static final class Utils {
        public static ClassDescriptor toOjbClassDescriptor(final ClassDescriptorFixture fixture) {
            final List<FieldDescriptorFixture> fieldDescriptors = List.of(fixture.fieldDescriptors());
            return TestOjbMetadataUtils.createMockClassDescriptor(fixture.mappedClass(), fixture.table(),
                    fieldDescriptors, FieldDescriptorFixture.Utils::toOjbFieldDescriptor);
        }
    }

}
