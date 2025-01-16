package edu.cornell.kfs.tax.batch.service.impl;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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

import edu.cornell.kfs.tax.batch.annotation.ExtractableTaxDto;
import edu.cornell.kfs.tax.batch.annotation.ExtractionSource;
import edu.cornell.kfs.tax.batch.annotation.TaxDtoField;
import edu.cornell.kfs.tax.batch.dto.util.TaxDtoExtractorDefinition;
import edu.cornell.kfs.tax.batch.dto.util.TaxDtoFieldExtractor;
import edu.cornell.kfs.tax.batch.service.TaxTableMetadataLookupService;

public class TaxTableMetadataLookupServiceOjbImpl extends PersistenceServiceStructureImplBase
        implements TaxTableMetadataLookupService {

    @Override
    public <T> TaxDtoExtractorDefinition<T> getDtoExtractionMetadata(final Class<T> dtoClass) {
        Validate.notNull(dtoClass, "dtoClass cannot be null");
        final ExtractableTaxDto extractionInfo = getAndValidateExtractionAnnotation(dtoClass);
        return null;
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
        final List<Field> dtoFields = getAnnotatedDtoFields(dtoClass);
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

    private List<Field> getAnnotatedDtoFields(final Class<?> dtoClass) {
        return Arrays.stream(dtoClass.getDeclaredFields())
                .filter(field -> field.getAnnotation(TaxDtoField.class) != null)
                .sorted(Comparator.comparing(Field::getName))
                .collect(Collectors.toUnmodifiableList());
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
        final FailableBiFunction<ResultSet, String, Object, SQLException> columnValueExtractor =
                generateColumnValueExtractor(fieldDescriptor);
        final FieldConversion fieldConversion = fieldDescriptor.getFieldConversion();
        
        
        if (fieldConversion != null) {
            
        }
        
        return null;
    }

    private FailableBiFunction<ResultSet, String, Object, SQLException> generateColumnValueExtractor(
            final FieldDescriptor fieldDescriptor) {ResultSet rs = null;
        final int jdbcType = fieldDescriptor.getJdbcType().getType();
        switch (jdbcType) {
            case Types.VARCHAR:
            case Types.CHAR:
                return ResultSet::getString;

            case Types.INTEGER:
                return ResultSet::getInt;

            case Types.BIGINT:
                return ResultSet::getLong;

            case Types.DECIMAL:
                return ResultSet::getBigDecimal;

            case Types.DATE:
                return ResultSet::getDate;

            case Types.TIMESTAMP:
                return ResultSet::getTimestamp;

            default:
                throw new IllegalStateException("This service doesn't support extracting JDBC Type " + jdbcType);
        }
    }

}
