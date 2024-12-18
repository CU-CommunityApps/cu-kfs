package edu.cornell.kfs.tax.batch.service.impl;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.sys.KFSConstants;

import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;

import edu.cornell.kfs.tax.batch.CUTaxBatchConstants.TaxOutputFieldType;
import edu.cornell.kfs.tax.batch.dto.SprintaxInfo1042S;
import edu.cornell.kfs.tax.batch.xml.TaxOutputDefinition;
import edu.cornell.kfs.tax.batch.xml.TaxOutputField;
import edu.cornell.kfs.tax.batch.xml.TaxOutputSection;
import edu.cornell.kfs.tax.util.TaxUtils;

public abstract class TaxFileRowWriterSprintaxBase extends TaxFileRowWriterBase<SprintaxInfo1042S> {

    private final FileOutputStream outputStream;
    private final OutputStreamWriter streamWriter;

    protected final ICSVWriter csvWriter;
    protected final String outputFileName;
    protected final String taxFileType;
    protected final DecimalFormat amountFormat;
    protected final DecimalFormat percentFormat;

    protected TaxFileRowWriterSprintaxBase(final String outputFileName, final String taxFileType,
            final TaxOutputDefinition taxOutputDefinition) throws IOException {
        super(taxOutputDefinition);
        Validate.notBlank(outputFileName, "outputFileName cannot be blank");
        Validate.notBlank(taxFileType, "taxFileType cannot be blank");

        FileOutputStream outputStream = null;
        OutputStreamWriter streamWriter = null;
        ICSVWriter csvWriter = null;
        boolean success = false;

        try {
            outputStream = new FileOutputStream(outputFileName);
            streamWriter = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
            csvWriter = buildCSVWriter(streamWriter, taxOutputDefinition.getFieldSeparator());
            this.outputStream = outputStream;
            this.streamWriter = streamWriter;
            this.csvWriter = csvWriter;
            this.outputFileName = outputFileName;
            this.taxFileType = taxFileType;
            this.amountFormat = TaxUtils.buildDefaultAmountFormatForFileOutput();
            this.percentFormat = TaxUtils.buildDefaultPercentFormatForSprintaxFileOutput();
            success = true;
        } finally {
            if (!success) {
                IOUtils.closeQuietly(csvWriter, streamWriter, outputStream);
            }
        }
    }

    private static ICSVWriter buildCSVWriter(final Writer fileWriter, final String separator) {
        Validate.isTrue(StringUtils.length(separator) == 1, "separator should have been exactly 1 character long");
        return new CSVWriterBuilder(fileWriter)
                .withSeparator(separator.charAt(0))
                .withLineEnd(KFSConstants.NEWLINE)
                .build();
    }

    @Override
    public void close() throws IOException {
        IOUtils.closeQuietly(csvWriter, streamWriter, outputStream);
    }

    @Override
    public String getTaxFileType() {
        return taxFileType;
    }

    @Override
    public void writeHeaderRow(final String sectionName) throws IOException {
        Validate.notBlank(sectionName, "sectionName cannot be blank");

        final TaxOutputSection section = getSection(sectionName);
        Validate.validState(section.isHasHeaderRow(),
                "Section does not treat its field names as header labels: %s", sectionName);

        final String[] headers = section.getFields().stream()
                .map(TaxOutputField::getName)
                .toArray(String[]::new);
        csvWriter.writeNext(headers);
    }

    protected Map.Entry<String, String> buildEntry(final Enum<?> key, final String value) {
        return Map.entry(key.name(), StringUtils.defaultString(value));
    }

    protected Map.Entry<String, String> buildEntryForAmount(final Enum<?> key, final KualiDecimal value) {
        return value != null
                ? Map.entry(key.name(), amountFormat.format(value))
                : buildEntry(key, KFSConstants.EMPTY_STRING);
    }

    protected Map.Entry<String, String> buildNegatedEntryForAmount(final Enum<?> key, final KualiDecimal value) {
        return value != null
                ? buildEntryForAmount(key, value.negated())
                : buildEntry(key, KFSConstants.EMPTY_STRING);
    }

    protected Map.Entry<String, String> buildEntryForPercent(final Enum<?> key, final KualiDecimal value) {
        return value != null
                ? Map.entry(key.name(), percentFormat.format(value))
                : buildEntry(key, KFSConstants.EMPTY_STRING);
    }

    protected void writeCsvDataRow(final Map<String, String> derivedDataValues,
            final String sectionName) throws IOException {
        final TaxOutputSection section = getSection(sectionName);
        final Stream.Builder<String> rowData = Stream.builder();

        for (final TaxOutputField taxField : section.getFields()) {
            String fieldValue;
            if (taxField.getType() == TaxOutputFieldType.STATIC) {
                fieldValue = taxField.getValue();
            } else if (taxField.getType() == TaxOutputFieldType.DERIVED) {
                fieldValue = derivedDataValues.get(taxField.getKey());
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

        final String[] csvRow = rowData.build().toArray(String[]::new);
        csvWriter.writeNext(csvRow);
    }

}
