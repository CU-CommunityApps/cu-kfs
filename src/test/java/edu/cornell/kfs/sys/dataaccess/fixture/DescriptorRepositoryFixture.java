package edu.cornell.kfs.sys.dataaccess.fixture;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.ojb.broker.metadata.DescriptorRepository;

import edu.cornell.kfs.sys.dataaccess.util.TestOjbMetadataUtils;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DescriptorRepositoryFixture {

    ClassDescriptorFixture[] classDescriptors();



    public static final class Utils {

        public static DescriptorRepository toOjbDescriptorRepository(final DescriptorRepositoryFixture fixture) {
            return createMockDescriptorRepository(List.of(fixture.classDescriptors()));
        }

        public static DescriptorRepository toCombinedOjbDescriptorRepository(
                final Collection<DescriptorRepositoryFixture> fixtures) {
            final List<ClassDescriptorFixture> combinedClassDescriptors = fixtures.stream()
                    .map(DescriptorRepositoryFixture::classDescriptors)
                    .flatMap(Arrays::stream)
                    .collect(Collectors.toUnmodifiableList());

            return createMockDescriptorRepository(combinedClassDescriptors);
        }

        private static DescriptorRepository createMockDescriptorRepository(
                final List<ClassDescriptorFixture> classDescriptors) {
            return TestOjbMetadataUtils.createMockDescriptorRepository(classDescriptors,
                    ClassDescriptorFixture.Utils::toOjbClassDescriptor);
        }

    }

}
