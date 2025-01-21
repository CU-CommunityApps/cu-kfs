package edu.cornell.kfs.tax.batch.service.impl;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.JDBCType;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.ojb.broker.accesslayer.conversions.FieldConversion;
import org.apache.ojb.broker.metadata.ClassDescriptor;
import org.apache.ojb.broker.metadata.FieldDescriptor;
import org.kuali.kfs.krad.bo.BusinessObject;
import org.kuali.kfs.krad.service.impl.PersistenceServiceStructureImplBase;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.tax.batch.annotation.TaxBusinessObjectMapping;
import edu.cornell.kfs.tax.batch.annotation.TaxDto;
import edu.cornell.kfs.tax.batch.annotation.TaxDtoField;
import edu.cornell.kfs.tax.batch.metadata.TaxDtoFieldConverter;
import edu.cornell.kfs.tax.batch.metadata.TaxDtoFieldDefinition;
import edu.cornell.kfs.tax.batch.metadata.TaxDtoMappingDefinition;
import edu.cornell.kfs.tax.batch.service.TaxTableMetadataLookupService;

public class TaxTableMetadataLookupServiceOjbImpl extends PersistenceServiceStructureImplBase
        implements TaxTableMetadataLookupService {

    private static final Pattern NAME_CHARS_LOWERCASE_FIRST_UPPERCASE_SECOND = Pattern.compile("^[a-z][A-Z].*$");
    private static final String GET_METHOD_PREFIX = "get";
    private static final String SET_METHOD_PREFIX = "set";
    private static final String ACCEPT_METHOD = "accept";
    private static final String APPLY_METHOD = "apply";
    private static final String GET_METHOD = "get";

    private final MethodHandles.Lookup lookup;

    public TaxTableMetadataLookupServiceOjbImpl() {
        super();
        this.lookup = MethodHandles.lookup();
    }

    @Override
    public <T> TaxDtoMappingDefinition<T> getDatabaseMappingMetadataForDto(final Class<T> dtoClass) {
        Validate.notNull(dtoClass, "dtoClass cannot be null");
        Validate.isTrue(Modifier.isPublic(dtoClass.getModifiers()), "dtoClass must be declared as public", dtoClass);
        final TaxDto dtoInfo = getAndValidateTaxDtoAnnotation(dtoClass);
        final List<Pair<Class<? extends BusinessObject>, String>> businessObjectMappings =
                generateBusinessObjectMappings(dtoInfo);
        final List<TaxDtoFieldDefinition<T, ?>> fieldDefinitions = generateFieldDefinitions(dtoClass, dtoInfo);
        final Supplier<T> dtoConstructor = generateLambdaForDefaultDtoConstructor(dtoClass);
        return new TaxDtoMappingDefinition<>(dtoClass, dtoConstructor, businessObjectMappings, fieldDefinitions);
    }

    private TaxDto getAndValidateTaxDtoAnnotation(final Class<?> dtoClass) {
        final TaxDto dtoInfo = dtoClass.getAnnotation(TaxDto.class);
        Validate.notNull(dtoInfo, "dtoClass %s does not have the TaxDto annotation", dtoClass);

        final TaxBusinessObjectMapping[] mappedObjects = dtoInfo.mappedBusinessObjects();
        Validate.notEmpty(mappedObjects, "Annotation from dtoClass %s does not specify any business objects to map to",
                dtoClass);

        final Set<Class<? extends BusinessObject>> boClasses = new HashSet<>();
        final Set<String> tableAliases = new HashSet<>();
        boolean foundBlankAlias = false;
        for (final TaxBusinessObjectMapping mappedObject : mappedObjects) {
            Validate.isTrue(boClasses.add(mappedObject.businessObjectClass()),
                    "Annotation from dtoClass %s contained duplicate mapped business objects", dtoClass);
            if (StringUtils.isNotBlank(mappedObject.tableAliasForQuery())) {
                Validate.isTrue(
                        tableAliases.add(StringUtils.upperCase(mappedObject.tableAliasForQuery(), Locale.US)),
                        "Annotation from dtoClass %s contained duplicate query table aliases (case-insensitive)",
                        dtoClass);
            } else {
                foundBlankAlias = true;
            }
        }

        Validate.isTrue(mappedObjects.length == 1 || !foundBlankAlias,
                "Annotation from dtoClass %s cannot specify a blank table alias when mapping to multiple BOs",
                dtoClass);

        return dtoInfo;
    }

    private List<Pair<Class<? extends BusinessObject>, String>> generateBusinessObjectMappings(final TaxDto dtoInfo) {
        return Arrays.stream(dtoInfo.mappedBusinessObjects())
                .map(this::generateBusinessObjectMapping)
                .collect(Collectors.toUnmodifiableList());
    }

    private Pair<Class<? extends BusinessObject>, String> generateBusinessObjectMapping(
            final TaxBusinessObjectMapping boMapping) {
        return Pair.of(boMapping.businessObjectClass(), boMapping.tableAliasForQuery());
    }

    private <T> List<TaxDtoFieldDefinition<T, ?>> generateFieldDefinitions(final Class<T> dtoClass,
            final TaxDto dtoInfo) {
        final int objectMappingCount = dtoInfo.mappedBusinessObjects().length;
        Validate.validState(objectMappingCount > 0, "Annotation on dtoClass %s should have had 1 or more BO mappings",
                dtoClass);

        final Function<Class<? extends BusinessObject>, Pair<TaxBusinessObjectMapping, ClassDescriptor>> objectMapper =
                getObjectMapper(dtoInfo);
        final List<Field> dtoFields = getAnnotatedDtoFields(dtoClass);
        Validate.validState(dtoFields.size() > 0,
                "dtoClass %s does not have any properly annotated fields", dtoClass);

        final Stream.Builder<TaxDtoFieldDefinition<T, ?>> fieldDefinitions = Stream.builder();

        for (final Field dtoField : dtoFields) {
            final TaxDtoFieldDefinition<T, ?> fieldDefinition = generateFieldDefinition(
                    dtoClass, dtoField, objectMapper);
            Validate.validState(!fieldDefinition.isUpdatable() || objectMappingCount == 1,
                    "Unsupported configuration: Cannot mark field %s as updatable because the annotation for dtoClass "
                            + "%s specifies more than one mapped business object", dtoField.getName(), dtoClass);
            fieldDefinitions.add(fieldDefinition);
        }

        return fieldDefinitions.build()
                .collect(Collectors.toUnmodifiableList());
    }

    private Function<Class<? extends BusinessObject>, Pair<TaxBusinessObjectMapping, ClassDescriptor>> getObjectMapper(
            final TaxDto dtoInfo) {
        final Map<Class<? extends BusinessObject>, Pair<TaxBusinessObjectMapping, ClassDescriptor>> mappings =
                getObjectMappings(dtoInfo);
        if (mappings.size() != 1) {
            return mappings::get;
        } else {
            final Pair<TaxBusinessObjectMapping, ClassDescriptor> singleMapping = mappings.values().iterator().next();
            final Class<? extends BusinessObject> singleKey = singleMapping.getLeft().businessObjectClass();
            return key -> singleKey.equals(key) || BusinessObject.class.equals(key) ? singleMapping : null;
        }
    }

    private Map<Class<? extends BusinessObject>, Pair<TaxBusinessObjectMapping, ClassDescriptor>> getObjectMappings(
            final TaxDto dtoInfo) {
        return Arrays.stream(dtoInfo.mappedBusinessObjects())
                .collect(Collectors.toUnmodifiableMap(
                        TaxBusinessObjectMapping::businessObjectClass,
                        mappedObject -> Pair.of(
                                mappedObject, super.getClassDescriptor(mappedObject.businessObjectClass()))));
    }

    private List<Field> getAnnotatedDtoFields(final Class<?> dtoClass) {
        return Arrays.stream(dtoClass.getDeclaredFields())
                .filter(field -> field.getAnnotation(TaxDtoField.class) != null)
                .sorted(Comparator.comparing(Field::getName))
                .collect(Collectors.toUnmodifiableList());
    }



    private <T> TaxDtoFieldDefinition<T, ?> generateFieldDefinition(final Class<T> dtoClass, final Field dtoField,
            final Function<Class<? extends BusinessObject>, Pair<TaxBusinessObjectMapping, ClassDescriptor>> mapper) {
        return generateFieldExtractor(dtoClass, dtoField.getType(), dtoField, mapper);
    }

    private <T, U> TaxDtoFieldDefinition<T, U> generateFieldExtractor(final Class<T> dtoClass,
            final Class<U> fieldType, final Field dtoField,
            final Function<Class<? extends BusinessObject>, Pair<TaxBusinessObjectMapping, ClassDescriptor>> mapper) {
        final TaxDtoField fieldInfo = dtoField.getAnnotation(TaxDtoField.class);
        Validate.validState(fieldInfo != null, "DTO field %s should have been annotated with TaxDtoField",
                dtoField.getName());

        final Pair<TaxBusinessObjectMapping, ClassDescriptor> boInfo = mapper.apply(fieldInfo.mappedBusinessObject());
        Validate.validState(boInfo != null, "DTO field %s does not map to one of the expected business objects "
                + "(or did not specify an explicit mapping for a DTO mapped to multiple BOs)", dtoField.getName());

        final FieldDescriptor fieldDescriptor = getFieldDescriptor(dtoField, fieldInfo, boInfo.getRight());
        final JDBCType jdbcType = JDBCType.valueOf(fieldDescriptor.getJdbcType().getType());
        final String columnLabel = generateColumnLabelForQueryUsage(fieldDescriptor, boInfo.getLeft());
        final Optional<TaxDtoFieldConverter> fieldConverter = generateFieldConverter(fieldDescriptor);

        final Function<T, U> propertyGetter = generateLambdaForDtoPropertyGetter(dtoClass, fieldType, dtoField);
        final BiConsumer<T, U> propertySetter = generateLambdaForDtoPropertySetter(dtoClass, fieldType, dtoField);

        return new TaxDtoFieldDefinition<>(dtoClass, fieldType, dtoField.getName(), propertyGetter, propertySetter,
                fieldInfo.updatable(), jdbcType, columnLabel, fieldConverter);
    }

    private FieldDescriptor getFieldDescriptor(final Field dtoField, final TaxDtoField fieldInfo,
            final ClassDescriptor boDescriptor) {
        final String fieldName;
        if (StringUtils.isNotBlank(fieldInfo.actualBOField())) {
            fieldName = fieldInfo.actualBOField();
        } else {
            fieldName = dtoField.getName();
        }
        final FieldDescriptor fieldDescriptor = boDescriptor.getFieldDescriptorByName(fieldName);
        Validate.validState(fieldDescriptor != null, "Could not find field descriptor: %s", fieldName);
        return fieldDescriptor;
    }

    private String generateColumnLabelForQueryUsage(final FieldDescriptor fieldDescriptor,
            final TaxBusinessObjectMapping boMapping) {
        final String tableAlias = boMapping.tableAliasForQuery();
        final String columnName = fieldDescriptor.getColumnName();
        return StringUtils.isNotBlank(tableAlias)
                ? StringUtils.join(tableAlias, KFSConstants.DELIMITER, columnName)
                : columnName;
    }

    private Optional<TaxDtoFieldConverter> generateFieldConverter(final FieldDescriptor fieldDescriptor) {
        final FieldConversion fieldConversion = fieldDescriptor.getFieldConversion();
        return Optional.ofNullable(fieldConversion)
                .map(TaxDtoFieldConverterOjbImpl::new);
    }



    /*
     * Dynamically creates a BiConsumer lambda for handling the setter method of a DTO property,
     * using MethodHandle-related objects instead of reflection for better performance.
     * The returned BiConsumer expects the DTO as the first argument and the property value
     * as the second argument.
     * 
     * This solution is based upon the code from the following Stack Overflow article,
     * including the part for making things compatible with primitive property values:
     * 
     * https://stackoverflow.com/questions/61592566/create-biconsumer-from-lambdametafactory
     */
    private <T, U> BiConsumer<T, U> generateLambdaForDtoPropertySetter(final Class<T> dtoClass,
            final Class<U> fieldType, final Field dtoField) {
        try {
            final String setterMethodName = determineSetterMethodName(dtoField);
            verifyBeanPropertyMethodIsPublic(dtoClass, setterMethodName, fieldType);

            final MethodHandle setterMethodHandle = lookup.findVirtual(dtoClass, setterMethodName,
                    MethodType.methodType(void.class, fieldType));
            final MethodType adjustedSetterMethodType = setterMethodHandle.type()
                    .wrap()
                    .changeReturnType(void.class);

            final CallSite callSite = LambdaMetafactory.metafactory(
                    lookup,
                    ACCEPT_METHOD,
                    MethodType.methodType(BiConsumer.class),
                    adjustedSetterMethodType.erase(),
                    setterMethodHandle,
                    adjustedSetterMethodType);

            final MethodHandle biConsumerFactory = callSite.getTarget();
            return (BiConsumer<T, U>) biConsumerFactory.invoke();

        } catch (final RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        } catch (final Throwable t) {
            throw new AssertionError(t);
        }
    }

    /*
     * See the related generateLambdaForDtoPropertySetter() method for further details
     * on the logic that is being performed.
     */
    private <T, U> Function<T, U> generateLambdaForDtoPropertyGetter(final Class<T> dtoClass,
            final Class<U> fieldType, final Field dtoField) {
        try {
            final String getterMethodName = determineGetterMethodName(dtoField);
            verifyBeanPropertyMethodIsPublic(dtoClass, getterMethodName);

            final MethodHandle getterMethodHandle = lookup.findVirtual(dtoClass, getterMethodName,
                    MethodType.methodType(fieldType));
            final MethodType adjustedGetterMethodType = getterMethodHandle.type().wrap();

            final CallSite callSite = LambdaMetafactory.metafactory(
                    lookup,
                    APPLY_METHOD,
                    MethodType.methodType(Function.class),
                    adjustedGetterMethodType.erase(),
                    getterMethodHandle,
                    adjustedGetterMethodType);

            final MethodHandle functionFactory = callSite.getTarget();
            return (Function<T, U>) functionFactory.invoke();

        } catch (final RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        } catch (final Throwable t) {
            throw new AssertionError(t);
        }
    }

    private String determineGetterMethodName(final Field dtoField) {
        return determineBeanPropertyMethodName(dtoField, GET_METHOD_PREFIX);
    }

    private String determineSetterMethodName(final Field dtoField) {
        return determineBeanPropertyMethodName(dtoField, SET_METHOD_PREFIX);
    }

    private String determineBeanPropertyMethodName(final Field dtoField, final String beanMethodPrefix) {
        final String fieldName = dtoField.getName();
        if (NAME_CHARS_LOWERCASE_FIRST_UPPERCASE_SECOND.matcher(fieldName).matches()) {
            return beanMethodPrefix + fieldName;
        } else {
            return beanMethodPrefix + StringUtils.capitalize(fieldName);
        }
    }

    private void verifyBeanPropertyMethodIsPublic(final Class<?> dtoClass, final String methodName,
            final Class<?>... parameterTypes) {
        try {
            Method beanPropertyMethod = dtoClass.getDeclaredMethod(methodName, parameterTypes);
            Validate.validState(Modifier.isPublic(beanPropertyMethod.getModifiers()),
                    "Bean accessor method %s on dtoClass %s is not declared as public", methodName, dtoClass);
        } catch (final NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }



    private <T> Supplier<T> generateLambdaForDefaultDtoConstructor(final Class<T> dtoClass) {
        try {
            verifyNoArgConstructorIsAvailableAndPublic(dtoClass);

            final MethodHandle constructorHandle = lookup.findConstructor(dtoClass, MethodType.methodType(void.class));
            final MethodType constructorType = constructorHandle.type();

            final CallSite callSite = LambdaMetafactory.metafactory(
                    lookup,
                    GET_METHOD,
                    MethodType.methodType(Supplier.class),
                    constructorType.erase(),
                    constructorHandle,
                    constructorType);

            final MethodHandle supplierFactory = callSite.getTarget();
            return (Supplier<T>) supplierFactory.invoke();

        } catch (final RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        } catch (final Throwable t) {
            throw new AssertionError(t);
        }
    }

    private void verifyNoArgConstructorIsAvailableAndPublic(final Class<?> dtoClass) {
        try {
            final Constructor<?> noArgConstructor = dtoClass.getDeclaredConstructor();
            Validate.validState(Modifier.isPublic(noArgConstructor.getModifiers()),
                    "Default no-arg constructor on dtoClass %s is not declared as public", dtoClass);
        } catch (final NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

}
