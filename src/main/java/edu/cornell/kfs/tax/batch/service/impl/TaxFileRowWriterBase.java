package edu.cornell.kfs.tax.batch.service.impl;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.kuali.kfs.sys.KFSConstants;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

import edu.cornell.kfs.tax.batch.CUTaxBatchConstants.TaxOutputFieldType;
import edu.cornell.kfs.tax.batch.dataaccess.TaxDtoFieldEnum;
import edu.cornell.kfs.tax.batch.service.TaxFileRowWriter;
import edu.cornell.kfs.tax.batch.xml.TaxOutputDefinitionV2;
import edu.cornell.kfs.tax.batch.xml.TaxOutputFieldV2;
import edu.cornell.kfs.tax.batch.xml.TaxOutputSectionV2;

public abstract class TaxFileRowWriterBase<T> implements TaxFileRowWriter<T> {

    protected final TaxOutputDefinitionV2 taxOutputDefinition;
    protected final Map<String, TaxOutputSectionV2> taxOutputSections;
    protected final Map<String, TaxDtoFieldEnum> validDerivedFields;
    protected final Map<TaxDtoFieldEnum, BiFunction<String, Object, String>> formatters;
    protected final BiFunction<String, Object, String> defaultFormatter;

    protected TaxFileRowWriterBase(final TaxOutputDefinitionV2 taxOutputDefinition,
            final Class<? extends TaxDtoFieldEnum> fieldEnumClass) {
        Validate.notNull(taxOutputDefinition, "taxOutputDefinition cannot be null");
        Validate.notNull(fieldEnumClass, "fieldEnumClass cannot be null");
        Validate.isTrue(fieldEnumClass.getEnumConstants() != null, "fieldEnumClass should have been an enum class");
        this.taxOutputDefinition = taxOutputDefinition;
        this.taxOutputSections = taxOutputDefinition.getSections().stream()
                .collect(Collectors.toUnmodifiableMap(TaxOutputSectionV2::getName, Function.identity()));
        this.validDerivedFields = Arrays.stream(fieldEnumClass.getEnumConstants())
                .collect(Collectors.toUnmodifiableMap(TaxDtoFieldEnum::getFieldName, Function.identity()));
        this.formatters = buildFormattersMap();
        this.defaultFormatter = this::performDefaultFormatting;
    }

    protected abstract Map<TaxDtoFieldEnum, BiFunction<String, Object, String>> buildFormattersMap();

    protected String performDefaultFormatting(final String sectionName, final Object value) {
        return value != null ? value.toString() : KFSConstants.EMPTY_STRING;
    }

    @Override
    public void writeHeaderRow(final String sectionName) throws IOException {
        Validate.notBlank(sectionName, "sectionName cannot be blank");

        final TaxOutputSectionV2 section = getSection(sectionName);
        Validate.validState(section.isHasHeaderRow(),
                "Section does not treat its field names as header labels: %s", sectionName);

        final Stream<String> headers = section.getFields().stream()
                .map(TaxOutputFieldV2::getName);

        writeHeaderRow(sectionName, headers);
    }

    protected abstract void writeHeaderRow(final String sectionName,
            final Stream<String> headerData) throws IOException;

    protected TaxOutputSectionV2 getSection(final String sectionName) {
        final TaxOutputSectionV2 section = taxOutputSections.get(sectionName);
        Validate.validState(section != null, "Could not find tax output section: %s", sectionName);
        return section;
    }

    @Override
    public void writeDataRow(final String sectionName, final T taxFileRowDto) throws IOException {
        Validate.notNull(taxFileRowDto, "taxFileRowDto cannot be null");
        Validate.notBlank(sectionName, "sectionName cannot be blank");

        final TaxOutputSectionV2 section = getSection(sectionName);
        final BeanWrapper wrappedDto = PropertyAccessorFactory.forBeanPropertyAccess(taxFileRowDto);
        final Stream.Builder<String> rowData = Stream.builder();

        for (final TaxOutputFieldV2 taxField : section.getFields()) {
            String fieldValue;
            if (taxField.getType() == TaxOutputFieldType.STATIC) {
                fieldValue = taxField.getValue();
            } else if (taxField.getType() == TaxOutputFieldType.DERIVED) {
                fieldValue = getDerivedFieldValue(sectionName, taxField.getKey(), wrappedDto);
            } else {
                throw new IllegalStateException("Unrecognized tax field type: " + taxField.getType());
            }

            fieldValue = StringUtils.defaultString(fieldValue);
            if (fieldValue.length() > taxField.getLength()) {
                fieldValue = StringUtils.left(fieldValue, taxField.getLength());
            } else if (section.isUseExactFieldLengths() && fieldValue.length() < taxField.getLength()) {
                fieldValue = StringUtils.rightPad(fieldValue, taxField.getLength(), ' ');
            }
            rowData.add(fieldValue);
        }

        writeDataRow(sectionName, rowData.build());
    }

    protected abstract void writeDataRow(final String sectionName, final Stream<String> rowData) throws IOException;

    protected String getDerivedFieldValue(final String sectionName, final String fieldName,
            final BeanWrapper wrappedDto) {
        final TaxDtoFieldEnum field = validDerivedFields.get(fieldName);
        Validate.validState(fieldName != null, "Invalid derived field name/key: %s", fieldName);
        final Object value = wrappedDto.getPropertyValue(fieldName);
        final BiFunction<String, Object, String> formatter = formatters.getOrDefault(field, defaultFormatter);
        return formatter.apply(sectionName, value);
    }

}
