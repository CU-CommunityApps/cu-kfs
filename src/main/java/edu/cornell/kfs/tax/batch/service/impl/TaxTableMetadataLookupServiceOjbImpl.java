package edu.cornell.kfs.tax.batch.service.impl;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.sql.JDBCType;
import java.sql.ResultSet;
import java.sql.SQLException;
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
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.function.FailableBiFunction;
import org.apache.ojb.broker.accesslayer.conversions.FieldConversion;
import org.apache.ojb.broker.metadata.ClassDescriptor;
import org.apache.ojb.broker.metadata.FieldDescriptor;
import org.kuali.kfs.krad.bo.BusinessObject;
import org.kuali.kfs.krad.service.impl.PersistenceServiceStructureImplBase;

import edu.cornell.kfs.sys.util.CuFailableTriConsumer;
import edu.cornell.kfs.tax.batch.annotation.ExtractableTaxDto;
import edu.cornell.kfs.tax.batch.annotation.ExtractionSource;
import edu.cornell.kfs.tax.batch.annotation.TaxDtoField;
import edu.cornell.kfs.tax.batch.annotation.UpdatableTaxDto;
import edu.cornell.kfs.tax.batch.dto.util.TaxDtoExtractorDefinition;
import edu.cornell.kfs.tax.batch.dto.util.TaxDtoFieldExtractor;
import edu.cornell.kfs.tax.batch.dto.util.TaxDtoFieldUpdater;
import edu.cornell.kfs.tax.batch.dto.util.TaxDtoUpdaterDefinition;
import edu.cornell.kfs.tax.batch.service.TaxTableMetadataLookupService;

public class TaxTableMetadataLookupServiceOjbImpl extends PersistenceServiceStructureImplBase
        implements TaxTableMetadataLookupService {

    private static final Pattern NAME_CHARS_LOWERCASE_FIRST_UPPERCASE_SECOND = Pattern.compile("^[a-z][A-Z].*$");
    private static final String GET_METHOD_PREFIX = "get";
    private static final String SET_METHOD_PREFIX = "set";
    private static final String ACCEPT_METHOD = "accept";
    private static final String APPLY_METHOD = "apply";

    private final MethodHandles.Lookup lookup;

    public TaxTableMetadataLookupServiceOjbImpl() {
        super();
        this.lookup = MethodHandles.lookup();
    }



    @Override
    public <T> TaxDtoExtractorDefinition<T> getDtoMetadataForSqlExtraction(final Class<T> dtoClass) {
        Validate.notNull(dtoClass, "dtoClass cannot be null");
        final ExtractableTaxDto extractionInfo = getAndValidateExtractionAnnotation(dtoClass);
        final List<TaxDtoFieldExtractor<T, ?>> fieldExtractors = generateFieldExtractors(dtoClass, extractionInfo);
        return new TaxDtoExtractorDefinition<>(dtoClass, extractionInfo.sourceBusinessObjects(), fieldExtractors);
    }

    private ExtractableTaxDto getAndValidateExtractionAnnotation(final Class<?> dtoClass) {
        final ExtractableTaxDto extractionInfo = dtoClass.getAnnotation(ExtractableTaxDto.class);
        Validate.notNull(extractionInfo, "dtoClass does not have the ExtractableTaxDto annotation");

        final ExtractionSource[] sourceObjects = extractionInfo.sourceBusinessObjects();
        Validate.isTrue(sourceObjects != null && sourceObjects.length > 0,
                "Annotation from dtoClass does not specify any source business objects to extract from");

        final Set<Class<? extends BusinessObject>> boClasses = new HashSet<>();
        final Set<String> tableAliases = new HashSet<>();
        boolean foundBlankAlias = false;
        for (final ExtractionSource sourceObject : sourceObjects) {
            Validate.isTrue(boClasses.add(sourceObject.businessObjectClass()),
                    "Annotation from dtoClass contained duplicate source business objects");
            if (StringUtils.isNotBlank(sourceObject.tableAliasForQuery())) {
                Validate.isTrue(
                        tableAliases.add(StringUtils.upperCase(sourceObject.tableAliasForQuery(), Locale.US)),
                        "Annotation from dtoClass contained duplicate query table aliases (case-insensitive)");
            } else {
                foundBlankAlias = true;
            }
        }

        Validate.isTrue(sourceObjects.length == 1 || !foundBlankAlias,
                "Annotation from dtoClass cannot specify a blank table alias when extracting from multiple BOs");

        return extractionInfo;
    }

    private <T> List<TaxDtoFieldExtractor<T, ?>> generateFieldExtractors(final Class<T> dtoClass,
            final ExtractableTaxDto extractionInfo) {
        final Map<Class<? extends BusinessObject>, ClassDescriptor> sourceBOMappings = getOJBMappings(extractionInfo);
        final Optional<Class<? extends BusinessObject>> singleSourceBO = sourceBOMappings.size() == 1
                ? Optional.of(sourceBOMappings.keySet().iterator().next()) : Optional.empty();
        final List<Field> dtoFields = getAnnotatedDtoFieldsForDataExtraction(dtoClass);
        final Stream.Builder<TaxDtoFieldExtractor<T, ?>> fieldExtractors = Stream.builder();
        Validate.validState(dtoFields.size() > 0, "dtoClass does not have any properly annotated fields");

        for (final Field dtoField : dtoFields) {
            final TaxDtoField fieldInfo = dtoField.getAnnotation(TaxDtoField.class);
            Validate.validState(fieldInfo != null, "DTO field should have been annotated but wasn't");

            final ClassDescriptor boDescriptor = getClassDescriptorForFieldSource(
                    fieldInfo, sourceBOMappings, singleSourceBO);
            final FieldDescriptor fieldDescriptor = getFieldDescriptor(dtoField, fieldInfo, boDescriptor);
            final TaxDtoFieldExtractor<T, ?> fieldExtractor = generateFieldExtractor(
                    dtoClass, dtoField, fieldDescriptor);
            fieldExtractors.add(fieldExtractor);
        }

        return fieldExtractors.build()
                .collect(Collectors.toUnmodifiableList());
    }

    private Map<Class<? extends BusinessObject>, ClassDescriptor> getOJBMappings(
            final ExtractableTaxDto extractionInfo) {
        return Arrays.stream(extractionInfo.sourceBusinessObjects())
                .map(ExtractionSource::businessObjectClass)
                .collect(Collectors.toUnmodifiableMap(boClass -> boClass, super::getClassDescriptor));
    }

    private List<Field> getAnnotatedDtoFieldsForDataExtraction(final Class<?> dtoClass) {
        return getAnnotatedDtoFields(dtoClass, this::isDtoFieldMappedForExtraction);
    }

    private List<Field> getAnnotatedDtoFieldsForDataUpdates(final Class<?> dtoClass) {
        return getAnnotatedDtoFields(dtoClass, this::isDtoFieldMappedForUpdates);
    }

    private List<Field> getAnnotatedDtoFields(final Class<?> dtoClass, final Predicate<Field> fieldFilter) {
        return Arrays.stream(dtoClass.getDeclaredFields())
                .filter(fieldFilter)
                .sorted(Comparator.comparing(Field::getName))
                .collect(Collectors.toUnmodifiableList());
    }

    private boolean isDtoFieldMappedForExtraction(final Field dtoField) {
        return dtoField.getAnnotation(TaxDtoField.class) != null;
    }

    private boolean isDtoFieldMappedForUpdates(final Field dtoField) {
        final TaxDtoField fieldAnnotation = dtoField.getAnnotation(TaxDtoField.class);
        return fieldAnnotation != null && fieldAnnotation.updatable();
    }

    private ClassDescriptor getClassDescriptorForFieldSource(final TaxDtoField fieldInfo,
            final Map<Class<? extends BusinessObject>, ClassDescriptor> sourceBOMappings,
            final Optional<Class<? extends BusinessObject>> singleSourceBO) {
        if (sourceBOMappings.size() == 1) {
            Validate.validState(
                    BusinessObject.class.equals(fieldInfo.source()) || singleSourceBO.get().equals(fieldInfo.source()),
                    "DTO field explicitly specified a source BO that the DTO had not marked as a valid source");
            return sourceBOMappings.get(singleSourceBO.get());
        } else {
            Validate.validState(sourceBOMappings.containsKey(fieldInfo.source()),
                    "DTO field on multi-BO-source DTO must explicitly specify a valid source BO");
            return sourceBOMappings.get(fieldInfo.source());
        }
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

    private <T> TaxDtoFieldExtractor<T, ?> generateFieldExtractor(final Class<T> dtoClass, final Field dtoField,
            final FieldDescriptor fieldDescriptor) {
        return generateFieldExtractor(dtoClass, dtoField.getType(), dtoField, fieldDescriptor);
    }

    private <T, U> TaxDtoFieldExtractor<T, U> generateFieldExtractor(final Class<T> dtoClass,
            final Class<U> fieldType, final Field dtoField, final FieldDescriptor fieldDescriptor) {
        final FailableBiFunction<ResultSet, String, U, SQLException> columnValueExtractor =
                generateColumnValueExtractor(fieldDescriptor, fieldType);
        final BiConsumer<T, U> propertySetter = generateLambdaForDtoPropertySetter(dtoClass, fieldType, dtoField);
        return new TaxDtoFieldExtractor<>(dtoClass, fieldDescriptor.getColumnName(), dtoField.getName(),
                columnValueExtractor, propertySetter);
    }

    private <U> FailableBiFunction<ResultSet, String, U, SQLException> generateColumnValueExtractor(
            final FieldDescriptor fieldDescriptor, final Class<U> fieldType) {
        final FieldConversion fieldConversion = fieldDescriptor.getFieldConversion();
        if (fieldConversion != null) {
            return (resultSet, columnLabel) -> {
                final Object columnValue = resultSet.getObject(columnLabel);
                final Object convertedValue = fieldConversion.sqlToJava(columnValue);
                return fieldType.cast(convertedValue);
            };
        } else {
            return (resultSet, columnLabel) -> resultSet.getObject(columnLabel, fieldType);
        }
    }

    /*
     * Dynamically creates a BiConsumer for handling the setter method of a DTO property,
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



    @Override
    public <T> TaxDtoUpdaterDefinition<T> getDtoMetadataForSqlUpdates(final Class<T> dtoClass) {
        Validate.notNull(dtoClass, "dtoClass cannot be null");
        final UpdatableTaxDto updateInfo = getAndValidateUpdaterAnnotation(dtoClass);
        final List<TaxDtoFieldUpdater<T, ?>> fieldUpdaters = generateFieldUpdaters(dtoClass, updateInfo);
        return new TaxDtoUpdaterDefinition<>(dtoClass, updateInfo.targetBusinessObject(), fieldUpdaters);
    }

    private UpdatableTaxDto getAndValidateUpdaterAnnotation(final Class<?> dtoClass) {
        final UpdatableTaxDto updateInfo = dtoClass.getAnnotation(UpdatableTaxDto.class);
        Validate.notNull(updateInfo, "dtoClass does not have the UpdatableTaxDto annotation");
        return updateInfo;
    }

    private <T> List<TaxDtoFieldUpdater<T, ?>> generateFieldUpdaters(final Class<T> dtoClass,
            final UpdatableTaxDto updateInfo) {
        final Class<? extends BusinessObject> targetBusinessObject = updateInfo.targetBusinessObject();
        final ClassDescriptor classDescriptor = super.getClassDescriptor(targetBusinessObject);
        final List<Field> dtoFields = getAnnotatedDtoFieldsForDataUpdates(dtoClass);
        final Stream.Builder<TaxDtoFieldUpdater<T, ?>> fieldUpdaters = Stream.builder();
        Validate.validState(dtoFields.size() > 0, "dtoClass does not have any properly annotated fields for update");

        for (final Field dtoField : dtoFields) {
            final TaxDtoField fieldInfo = dtoField.getAnnotation(TaxDtoField.class);
            Validate.validState(fieldInfo != null, "DTO field should have been annotated but wasn't");
            Validate.validState(fieldInfo.updatable(), "DTO field should have been marked as updatable but wasn't");
            final FieldDescriptor fieldDescriptor = getFieldDescriptor(dtoField, fieldInfo, classDescriptor);
            final TaxDtoFieldUpdater<T, ?> fieldExtractor = generateFieldUpdater(
                    dtoClass, dtoField, fieldDescriptor);
            fieldUpdaters.add(fieldExtractor);
        }

        return fieldUpdaters.build()
                .collect(Collectors.toUnmodifiableList());
    }

    private <T> TaxDtoFieldUpdater<T, ?> generateFieldUpdater(final Class<T> dtoClass, final Field dtoField,
            final FieldDescriptor fieldDescriptor) {
        return generateFieldUpdater(dtoClass, dtoField.getType(), dtoField, fieldDescriptor);
    }

    private <T, U> TaxDtoFieldUpdater<T, U> generateFieldUpdater(final Class<T> dtoClass,
            final Class<U> fieldType, final Field dtoField, final FieldDescriptor fieldDescriptor) {
        final CuFailableTriConsumer<ResultSet, String, U, SQLException> columnValueUpdater =
                generateColumnValueUpdater(fieldDescriptor, fieldType);
        final Function<T, U> propertyGetter = generateLambdaForDtoPropertyGetter(dtoClass, fieldType, dtoField);
        return new TaxDtoFieldUpdater<>(dtoClass, fieldDescriptor.getColumnName(), dtoField.getName(),
                columnValueUpdater, propertyGetter);
    }

    private <U> CuFailableTriConsumer<ResultSet, String, U, SQLException> generateColumnValueUpdater(
            final FieldDescriptor fieldDescriptor, final Class<U> fieldType) {
        final int jdbcTypeInteger = fieldDescriptor.getJdbcType().getType();
        final JDBCType jdbcType = JDBCType.valueOf(jdbcTypeInteger);
        final FieldConversion fieldConversion = fieldDescriptor.getFieldConversion();

        if (fieldConversion != null) {
            return (resultSet, columnLabel, propertyValue) -> {
                final Object convertedValue = fieldConversion.javaToSql(propertyValue);
                resultSet.updateObject(columnLabel, convertedValue, jdbcType);
            };
        } else {
            return (resultSet, columnLabel, propertyValue) -> {
                resultSet.updateObject(columnLabel, propertyValue, jdbcType);
            }; 
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

}
