package edu.cornell.kfs.tax.batch.service.impl;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.sys.KFSConstants;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

import edu.cornell.kfs.tax.CUTaxConstants;
import edu.cornell.kfs.tax.batch.CUTaxBatchConstants.TaxOutputFieldType;
import edu.cornell.kfs.tax.batch.dataaccess.TaxDtoFieldEnum;
import edu.cornell.kfs.tax.batch.service.TaxFileRowWriter;
import edu.cornell.kfs.tax.batch.xml.TaxOutputDefinitionV2;
import edu.cornell.kfs.tax.batch.xml.TaxOutputFieldV2;
import edu.cornell.kfs.tax.batch.xml.TaxOutputSectionV2;

public class TaxFileRowWriterImpl implements TaxFileRowWriter {

    @FunctionalInterface
    private static interface FieldValueFormatter {
        String getAndFormatFieldValue(final TaxFileRowWriterImpl rowWriter, final TaxOutputFieldV2 field,
                final BeanWrapper wrappedDto);
    }

    private static final Map<TaxOutputFieldType, FieldValueFormatter> FIELD_FORMATTERS = Map.ofEntries(
            Map.entry(TaxOutputFieldType.STATIC, TaxFileRowWriterImpl::getStaticValue),
            Map.entry(TaxOutputFieldType.STRING, TaxFileRowWriterImpl::getStringValue),
            Map.entry(TaxOutputFieldType.NORMALIZED_STRING, TaxFileRowWriterImpl::getNormalizedStringValue),
            Map.entry(TaxOutputFieldType.SENSITIVE_STRING, TaxFileRowWriterImpl::getPotentiallyMaskedValue),
            Map.entry(TaxOutputFieldType.INTEGER, TaxFileRowWriterImpl::getStringValue),
            Map.entry(TaxOutputFieldType.BOOLEAN, TaxFileRowWriterImpl::getFormattedBooleanValue),
            Map.entry(TaxOutputFieldType.DATE, TaxFileRowWriterImpl::getStringValue),
            Map.entry(TaxOutputFieldType.AMOUNT, TaxFileRowWriterImpl::getFormattedAmount),
            Map.entry(TaxOutputFieldType.NEGATED_AMOUNT, TaxFileRowWriterImpl::getNegatedAndFormattedAmount),
            Map.entry(TaxOutputFieldType.PERCENT, TaxFileRowWriterImpl::getFormattedPercent),
            Map.entry(TaxOutputFieldType.PLAIN_DECIMAL, TaxFileRowWriterImpl::getKualiDecimalInPlainStringFormat)
    );

    private final TaxOutputDefinitionV2 taxOutputDefinition;
    private final Map<String, TaxOutputSectionV2> taxOutputSections;
    private final Set<String> validNonStaticFields;
    private final DecimalFormat amountFormat;
    private final DecimalFormat percentFormat;
    private final boolean maskSensitiveData;
    private final WrappedCsvWriter wrappedCsvWriter;

    public TaxFileRowWriterImpl(final TaxOutputDefinitionV2 taxOutputDefinition,
            final Class<? extends TaxDtoFieldEnum> fieldEnumClass, final String outputFileName,
            final boolean maskSensitiveData) throws IOException {
        Validate.notNull(taxOutputDefinition, "taxOutputDefinition cannot be null");
        Validate.notNull(fieldEnumClass, "taxOutputDefinition cannot be null");
        Validate.notBlank(outputFileName, "outputFileName cannot be blank");

        this.taxOutputDefinition = taxOutputDefinition;
        this.taxOutputSections = taxOutputDefinition.getSections().stream()
                .collect(Collectors.toUnmodifiableMap(TaxOutputSectionV2::getName, Function.identity()));
        this.validNonStaticFields = Arrays.stream(fieldEnumClass.getEnumConstants())
                .map(TaxDtoFieldEnum::getFieldName)
                .collect(Collectors.toUnmodifiableSet());
        this.amountFormat = createDecimalFormat(taxOutputDefinition.getAmountFormat(),
                CUTaxConstants.DEFAULT_AMOUNT_FORMAT, CUTaxConstants.DEFAULT_AMOUNT_MAX_INT_DIGITS);
        this.percentFormat= createDecimalFormat(taxOutputDefinition.getPercentFormat(),
                CUTaxConstants.DEFAULT_PERCENT_FORMAT, CUTaxConstants.DEFAULT_PERCENT_MAX_INT_DIGITS);
        this.maskSensitiveData = maskSensitiveData;
        this.wrappedCsvWriter = new WrappedCsvWriter(outputFileName, taxOutputDefinition);
    }

    private DecimalFormat createDecimalFormat(final String formatString, final String defaultFormatString,
            final int maxIntegerDigits) {
        final String actualFormatString = StringUtils.defaultIfBlank(formatString, defaultFormatString);
        final DecimalFormat decimalFormat = new DecimalFormat(actualFormatString);
        decimalFormat.setMaximumIntegerDigits(maxIntegerDigits);
        return decimalFormat;
    }

    @Override
    public void close() throws IOException {
        wrappedCsvWriter.close();
    }

    @Override
    public void writeHeaderRow(final String sectionName) throws IOException {
        Validate.notBlank(sectionName, "sectionName cannot be blank");

        final TaxOutputSectionV2 section = getSection(sectionName);
        final Stream<String> headers = section.getFields().stream()
                .map(TaxOutputFieldV2::getName);

        writeCsvRow(headers);
    }

    private TaxOutputSectionV2 getSection(final String sectionName) {
        final TaxOutputSectionV2 section = taxOutputSections.get(sectionName);
        Validate.validState(section != null, "Could not find tax output section: %s", sectionName);
        return section;
    }

    private void writeCsvRow(final Stream<String> rowData) {
        final String[] csvRow = rowData.toArray(String[]::new);
        wrappedCsvWriter.writeNext(csvRow);
    }

    @Override
    public void writeDataRow(final String sectionName, final Object taxFileRowDto) throws IOException {
        Validate.notNull(taxFileRowDto, "taxFileRowDto cannot be null");
        Validate.notBlank(sectionName, "sectionName cannot be blank");

        final TaxOutputSectionV2 section = getSection(sectionName);
        final BeanWrapper wrappedDto = PropertyAccessorFactory.forBeanPropertyAccess(taxFileRowDto);
        final Stream.Builder<String> rowData = Stream.builder();

        for (final TaxOutputFieldV2 field : section.getFields()) {
            final FieldValueFormatter formatter = FIELD_FORMATTERS.get(field.getType());
            Validate.validState(formatter != null, "Invalid field type: %s", field.getType());
            Validate.validState(
                    field.getType() == TaxOutputFieldType.STATIC || validNonStaticFields.contains(field.getKey()),
                    "Invalid DTO field: %s", field.getKey());

            String fieldValue = StringUtils.defaultString(formatter.getAndFormatFieldValue(this, field, wrappedDto));
            if (!taxOutputDefinition.isIncludeQuotes()) {
                fieldValue = fieldValue.replace(taxOutputDefinition.getFieldSeparator(), KFSConstants.BLANK_SPACE);
            }

            if (fieldValue.length() > field.getLength()) {
                fieldValue = StringUtils.left(fieldValue, field.getLength());
            } else if (section.isUseExactFieldLengths() && fieldValue.length() < field.getLength()) {
                fieldValue = StringUtils.rightPad(fieldValue, field.getLength(), ' ');
            }

            rowData.add(fieldValue);
        }

        writeCsvRow(rowData.build());
    }

    private String getStaticValue(final TaxOutputFieldV2 field, final BeanWrapper wrappedDto) {
        return field.getValue();
    }

    private String getStringValue(final TaxOutputFieldV2 field, final BeanWrapper wrappedDto) {
        final Object value = wrappedDto.getPropertyValue(field.getKey());
        return (value != null) ? value.toString() : KFSConstants.EMPTY_STRING;
    }

    private String getNormalizedStringValue(final TaxOutputFieldV2 field, final BeanWrapper wrappedDto) {
        final String value = getStringValue(field, wrappedDto);
        return StringUtils.normalizeSpace(value);
    }

    private String getPotentiallyMaskedValue(final TaxOutputFieldV2 field, final BeanWrapper wrappedDto) {
        return maskSensitiveData ? field.getMask() : getStringValue(field, wrappedDto);
    }

    private String getFormattedBooleanValue(final TaxOutputFieldV2 field, final BeanWrapper wrappedDto) {
        final Boolean value = (Boolean) wrappedDto.getPropertyValue(field.getKey());
        if (value == null) {
            return KFSConstants.EMPTY_STRING;
        }
        return value.booleanValue() ? KRADConstants.YES_INDICATOR_VALUE : KRADConstants.NO_INDICATOR_VALUE;
    }

    private String getFormattedAmount(final TaxOutputFieldV2 field, final BeanWrapper wrappedDto) {
        final KualiDecimal value = getKualiDecimalValue(field, wrappedDto, false);
        return getFormattedKualiDecimalValue(value, amountFormat);
    }

    private String getNegatedAndFormattedAmount(final TaxOutputFieldV2 field, final BeanWrapper wrappedDto) {
        final KualiDecimal value = getKualiDecimalValue(field, wrappedDto, true);
        return getFormattedKualiDecimalValue(value, amountFormat);
    }

    private String getFormattedPercent(final TaxOutputFieldV2 field, final BeanWrapper wrappedDto) {
        final KualiDecimal value = getKualiDecimalValue(field, wrappedDto, false);
        return getFormattedKualiDecimalValue(value, percentFormat);
    }

    private String getKualiDecimalInPlainStringFormat(final TaxOutputFieldV2 field, final BeanWrapper wrappedDto) {
        final KualiDecimal value = getKualiDecimalValue(field, wrappedDto, false);
        return (value != null) ? value.bigDecimalValue().toPlainString() : KFSConstants.EMPTY_STRING;
    }

    private KualiDecimal getKualiDecimalValue(final TaxOutputFieldV2 field, final BeanWrapper wrappedDto,
            final boolean negate) {
        final KualiDecimal value = (KualiDecimal) wrappedDto.getPropertyValue(field.getKey());
        return (negate && value != null) ? value.negated() : value;
    }

    private String getFormattedKualiDecimalValue(final KualiDecimal value, final DecimalFormat formatter) {
        return (value != null) ? formatter.format(value.bigDecimalValue()) : KFSConstants.EMPTY_STRING;
    }

}
