package edu.cornell.kfs.tax.batch.service.impl;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.function.FailableRunnable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.KFSConstants;

import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;

import edu.cornell.kfs.core.api.util.CuCoreUtilities;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.CUKFSConstants.FileExtensions;
import edu.cornell.kfs.tax.CUTaxConstants;
import edu.cornell.kfs.tax.batch.TaxOutputDefinitionV2FileType;
import edu.cornell.kfs.tax.batch.dto.TaxFileRow;
import edu.cornell.kfs.tax.batch.xml.TaxOutputDefinition;
import edu.cornell.kfs.tax.batch.xml.TaxOutputField;
import edu.cornell.kfs.tax.batch.xml.TaxOutputSection;

public class TaxFileRowWriterServiceImpl /*implements TaxFileRowWriterService*/ {

    private static final Logger LOG = LogManager.getLogger();

    private String taxOutputDefinitionFile;
    private String outputDirectory;
    private String fileNamePrefix;
    private TaxOutputDefinitionV2FileType taxOutputDefinitionV2FileType;

    private TaxOutputHelper taxOutputHelper;

    //@Override
    public void doWithNewOutputFile(final int reportYear, final Date processingStartDate,
            final FailableRunnable<Exception> task) throws IOException, SQLException {
        Validate.validState(taxOutputHelper == null, "Another file writing operation is still in progress");
        Validate.notNull(processingStartDate, "processingStartDate cannot be null");
        Validate.notNull(task, "task cannot be null");

        final TaxOutputDefinition taxOutputDefinition = loadTaxOutputDefinition();
        final String outputFileName = generateFullOutputFileName(reportYear, processingStartDate);
        final String simpleFileName = StringUtils.substringAfterLast(outputFileName, CUKFSConstants.SLASH);
        LOG.info("doWithNewOutputFile, Opening file for output: {}", simpleFileName);
        LOG.debug("doWithNewOutputFile, Full path of new file: {}", outputFileName);

        try (
                final FileOutputStream outputStream = new FileOutputStream(outputFileName);
                final OutputStreamWriter streamWriter = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
                final ICSVWriter csvWriter = buildCSVWriter(streamWriter, taxOutputDefinition.getFieldSeparator());
        ) {
            taxOutputHelper = new TaxOutputHelper(taxOutputDefinition, csvWriter);
            task.run();
            if (csvWriter.checkError()) {
                throw new UncheckedIOException(csvWriter.getException());
            }
        } catch (final IOException | SQLException | RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        } finally {
            taxOutputHelper = null;
        }

        LOG.info("doWithNewOutputFile, Finished writing to file: {}", simpleFileName);
    }

    private String generateFullOutputFileName(final int reportYear, final Date processingStartDate) {
        final SimpleDateFormat dateFormat = new SimpleDateFormat(
                CUTaxConstants.FILENAME_SUFFIX_DATE_FORMAT, Locale.US);
        return StringUtils.join(
                outputDirectory, CUKFSConstants.SLASH, fileNamePrefix,
                reportYear, dateFormat.format(processingStartDate), FileExtensions.CSV);
    }

    private TaxOutputDefinition loadTaxOutputDefinition() throws IOException {
        try (final InputStream fileStream = CuCoreUtilities.getResourceAsStream(taxOutputDefinitionFile)) {
            final byte[] fileContents = IOUtils.toByteArray(fileStream);
            return taxOutputDefinitionV2FileType.parse(fileContents);
        }
    }

    private ICSVWriter buildCSVWriter(final Writer fileWriter, final String separator) {
        Validate.validState(StringUtils.length(separator) == 1,
                "The tax output definition should have specified a 1-character separator");
        return new CSVWriterBuilder(fileWriter)
                .withSeparator(separator.charAt(0))
                .withLineEnd(KFSConstants.NEWLINE)
                .build();
    }

    //@Override
    public void writeHeaderRow(final String sectionName) throws IOException {
        Validate.validState(taxOutputHelper != null, "Method was not invoked within a doWithNewOutputFile() task");
        Validate.notBlank(sectionName, "sectionName cannot be blank");

        final TaxOutputSection section = getSection(sectionName);
        Validate.validState(section.isHasHeaderRow(),
                "Section does not treat its field names as header labels: %s", sectionName);

        final String[] headers = section.getFields().stream()
                .map(TaxOutputField::getName)
                .toArray(String[]::new);
        taxOutputHelper.csvWriter.writeNext(headers);
    }

    //@Override
    public void writeDataRow(final TaxFileRow taxFileRow, final String sectionName) throws IOException {
        Validate.validState(taxOutputHelper != null, "Method was not invoked within a doWithNewOutputFile() task");
        Validate.notNull(taxFileRow, "taxFileRow cannot be null");
        Validate.notBlank(sectionName, "sectionName cannot be blank");

        final TaxOutputSection section = getSection(sectionName);
        final Map<String, String> rowValues = taxFileRow.generateFileRowValues(sectionName);
        final String[] csvCellValues = new String[section.getFields().size()];
        int index = -1;
        for (final TaxOutputField field : section.getFields()) {
            index++;
            final String value = getFieldValue(field, rowValues);
            final String adjustedValue = adjustFieldValue(field, value, section.isUseExactFieldLengths());
            csvCellValues[index] = adjustedValue;
        }

        taxOutputHelper.csvWriter.writeNext(csvCellValues);
    }

    private String getFieldValue(final TaxOutputField field, final Map<String, String> rowValues) {
        switch (field.getType()) {
            case STATIC :
                return field.getValue();

            case DERIVED :
                return rowValues.get(field.getKey());

            default :
                throw new IllegalStateException("Unrecognized field type: " + field.getType());
        }
    }

    private String adjustFieldValue(final TaxOutputField field, final String value, boolean useExactFieldLengths) {
        final String nonNullValue = StringUtils.defaultString(value);
        if (nonNullValue.length() > field.getLength()) {
            LOG.warn("adjustFieldValue, Truncating data value for field: {}", field.getName());
            return StringUtils.left(nonNullValue, field.getLength());
        } else if (useExactFieldLengths && nonNullValue.length() < field.getLength()) {
            return StringUtils.rightPad(nonNullValue, field.getLength());
        } else {
            return nonNullValue;
        }
    }

    private TaxOutputSection getSection(final String sectionName) {
        final TaxOutputSection section = taxOutputHelper.taxOutputSections.get(sectionName);
        Validate.isTrue(section != null, "Could not find tax output section: %s", sectionName);
        return section;
    }

    public void setTaxOutputDefinitionFile(final String taxOutputDefinitionFile) {
        this.taxOutputDefinitionFile = taxOutputDefinitionFile;
    }

    public void setOutputDirectory(final String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public void setFileNamePrefix(final String fileNamePrefix) {
        this.fileNamePrefix = fileNamePrefix;
    }

    public void setTaxOutputDefinitionV2FileType(final TaxOutputDefinitionV2FileType taxOutputDefinitionV2FileType) {
        this.taxOutputDefinitionV2FileType = taxOutputDefinitionV2FileType;
    }



    private static final class TaxOutputHelper {
        private final ICSVWriter csvWriter;
        private final Map<String, TaxOutputSection> taxOutputSections;

        private TaxOutputHelper(final TaxOutputDefinition taxOutputDefinition,
                final ICSVWriter csvWriter) {
            this.csvWriter = csvWriter;
            this.taxOutputSections = taxOutputDefinition.getSections().stream()
                    .collect(Collectors.toUnmodifiableMap(section -> section.getName(), section -> section));
        }
    }

}
