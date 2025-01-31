package edu.cornell.kfs.sys.dataaccess.util;

import java.sql.JDBCType;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.function.FailableSupplier;
import org.apache.ojb.broker.accesslayer.conversions.FieldConversion;
import org.apache.ojb.broker.accesslayer.conversions.FieldConversionDefaultImpl;
import org.apache.ojb.broker.metadata.ClassDescriptor;
import org.apache.ojb.broker.metadata.ClassNotPersistenceCapableException;
import org.apache.ojb.broker.metadata.DescriptorRepository;
import org.apache.ojb.broker.metadata.FieldDescriptor;
import org.apache.ojb.broker.metadata.JdbcType;
import org.apache.ojb.broker.metadata.MetadataManager;
import org.kuali.kfs.core.framework.persistence.ojb.conversion.OjbKualiEncryptDecryptFieldConversion;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import edu.cornell.kfs.sys.util.CuMockBuilder;

/**
 * Utility class for creating mock versions of OJB metadata objects, as well as for assisting
 * in the execution of operations that require calling the static MetadataManager.getInstance() method. 
 * 
 * Note that the mocked objects only cover a subset of the available object properties,
 * and mocked ClassDescriptor objects assume that the "schema" attribute has not been set.
 * 
 * To assist with unit tests, DescriptorRepository.iterator() will iterate over the class descriptors
 * in the same collection iteration order as the createMockDescriptorRepository() method's
 * Collection argument.
 * 
 * Also note that, for fields specifying OjbKualiEncryptDecryptFieldConversion, their converters
 * will be replaced with TestOjbKualiEncryptDecryptFieldConversion instead, which only performs
 * Base64 encoding/decoding rather than actual encryption/decryption.
 */
public final class TestOjbMetadataUtils {

    public static <E, T extends Throwable> E doWithMockMetadataManagerInstance(
            final DescriptorRepository mockRepository, final FailableSupplier<E, T> task) throws T {
        try (
                final MockedStatic<MetadataManager> mockedStaticManager = Mockito.mockStatic(MetadataManager.class)
        ) {
            final MetadataManager mockManagerInstance = createMockMetadataManager(mockRepository);
            mockedStaticManager.when(() -> MetadataManager.getInstance())
                    .thenReturn(mockManagerInstance);
            return task.get();
        }
    }

    public static MetadataManager createMockMetadataManager(final DescriptorRepository mockRepository) {
        return new CuMockBuilder<>(MetadataManager.class)
                .withReturn(MetadataManager::getGlobalRepository, mockRepository)
                .build();
    }

    public static <T> DescriptorRepository createMockDescriptorRepository(final Collection<T> classDescriptorFixtures,
            final Function<T, ClassDescriptor> fixtureConverter) {
        final List<ClassDescriptor> classDescriptors = createListOfOjbMetadataObjects(
                classDescriptorFixtures, fixtureConverter);
        final Map<String, ClassDescriptor> classDescriptorsMap = createMapOfOjbMetadataObjects(
                classDescriptors, ClassDescriptor::getClassNameOfObject);

        return new CuMockBuilder<>(DescriptorRepository.class)
                .withReturn(DescriptorRepository::getDescriptorTable, classDescriptorsMap)
                .withAnswer(DescriptorRepository::iterator, invocation -> classDescriptors.iterator())
                .withAnswer(
                        repository -> repository.getDescriptorFor(Mockito.any(Class.class)),
                        invocation -> {
                            final Class<?> mappedClass = invocation.getArgument(0);
                            return getExistingClassDescriptor(classDescriptorsMap, mappedClass.getName());
                        })
                .withAnswer(
                        repository -> repository.getDescriptorFor(Mockito.anyString()),
                        invocation -> getExistingClassDescriptor(classDescriptorsMap, invocation.getArgument(0)))
                .build();
    }

    private static ClassDescriptor getExistingClassDescriptor(final Map<String, ClassDescriptor> classDescriptorsMap,
            final String className) {
        final ClassDescriptor result = classDescriptorsMap.get(className);
        if (result == null) {
            throw new ClassNotPersistenceCapableException("No mock class descriptor exists for class: " + className);
        }
        return result;
    }

    public static <T> ClassDescriptor createMockClassDescriptor(final Class<?> mappedClass, final String tableName,
            final Collection<T> fieldDescriptorFixtures, final Function<T, FieldDescriptor> fixtureConverter) {
        final List<FieldDescriptor> fieldDescriptors = createListOfOjbMetadataObjects(
                fieldDescriptorFixtures, fixtureConverter);
        final Map<String, FieldDescriptor> fieldDescriptorsMap = createMapOfOjbMetadataObjects(
                fieldDescriptors, FieldDescriptor::getAttributeName);

        return new CuMockBuilder<>(ClassDescriptor.class)
                .withReturn(ClassDescriptor::getClassOfObject, mappedClass)
                .withReturn(ClassDescriptor::getClassNameOfObject, mappedClass.getName())
                .withReturn(ClassDescriptor::getFullTableName, tableName)
                .withAnswer(ClassDescriptor::getFieldDescriptions,
                            invocation -> fieldDescriptors.stream().toArray(FieldDescriptor[]::new))
                .withAnswer(
                        classDescriptor -> classDescriptor.getFieldDescriptorByName(Mockito.anyString()),
                        invocation -> fieldDescriptorsMap.get(invocation.getArgument(0)))
                .build();
    }

    public static FieldDescriptor createMockFieldDescriptor(final String fieldName, final String columnName,
            final JDBCType javaJdbcType, final Class<? extends FieldConversion> conversionClass) {
        final JdbcType ojbJdbcType = createMockOjbJdbcType(javaJdbcType);
        final FieldConversion fieldConversion = createFieldConversionInstance(conversionClass);

        return new CuMockBuilder<>(FieldDescriptor.class)
                .withReturn(FieldDescriptor::getAttributeName, fieldName)
                .withReturn(FieldDescriptor::getColumnName, columnName)
                .withReturn(FieldDescriptor::getColumnType, javaJdbcType.name())
                .withReturn(FieldDescriptor::getJdbcType, ojbJdbcType)
                .withReturn(FieldDescriptor::getFieldConversion, fieldConversion)
                .build();
    }

    public static JdbcType createMockOjbJdbcType(final JDBCType javaJdbcType) {
        return new CuMockBuilder<>(JdbcType.class)
                .withReturn(JdbcType::getType, javaJdbcType.getVendorTypeNumber())
                .build();
    }

    private static FieldConversion createFieldConversionInstance(
            final Class<? extends FieldConversion> conversionClass) {
        final Class<? extends FieldConversion> actualClass;
        if (conversionClass == null) {
            actualClass = FieldConversionDefaultImpl.class;
        } else if (OjbKualiEncryptDecryptFieldConversion.class.isAssignableFrom(conversionClass)) {
            actualClass = TestOjbKualiEncryptDecryptFieldConversion.class;
        } else {
            actualClass = conversionClass;
        }

        try {
            return actualClass.getConstructor().newInstance();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static <T, U> List<U> createListOfOjbMetadataObjects(final Collection<T> fixtures,
            final Function<T, U> fixtureConverter) {
        return fixtures.stream()
                .map(fixtureConverter)
                .collect(Collectors.toUnmodifiableList());
    }

    private static <K, V> Map<K, V> createMapOfOjbMetadataObjects(final Collection<V> metadataObjects,
            final Function<V, K> keyFinder) {
        return metadataObjects.stream()
                .collect(Collectors.toUnmodifiableMap(keyFinder, Function.identity()));
    }

}
